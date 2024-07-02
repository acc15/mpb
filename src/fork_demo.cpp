#include <iostream>
#include <string>
#include <exception>
#include <vector>
#include <unordered_map>
#include <unordered_set>
#include <ranges>

#include <boost/process/v2.hpp>
#include <boost/asio.hpp>
#include <boost/asio/experimental/channel.hpp>

enum class project_status {
    SUCCESS,
    ERROR
};

struct project_info {
    std::string name;
    std::unordered_set<std::string> deps;

    std::ostream& stream() const {
        return std::cout << "[" << name << "] ";
    }
};

struct project_completion {

    using keyset = std::unordered_set<std::string>;
    using map = std::unordered_map<std::string, project_status>;
    using pair = map::value_type;
    using channel = boost::asio::experimental::channel<void(boost::system::error_code)>;
    
    channel notifier;
    map results;

    project_completion(boost::asio::io_context& ctx): notifier(ctx), results() {}

    void done(const std::string& key, project_status status) {
        results.insert_or_assign(key, status);

        boost::system::error_code ec;
        while (notifier.try_send(ec));
    }

    const pair* find_pair(const std::string& key) const {
        map::const_iterator iter = results.find(key);
        return iter != results.end() ? &(*iter) : nullptr;
    }

    auto find_pairs(const keyset& keys) const {
        return keys 
            | std::views::transform(std::bind(&project_completion::find_pair, this, std::placeholders::_1))
            | std::views::filter([](const pair* p) { return p != nullptr; });
    } 

    const pair* find_any(const keyset& keys) const {
        for (const std::string& k: keys) {
            map::const_iterator it = results.find(k);
            if (it != results.end()) {
                return &(*it);
            }
        }
        return nullptr;
    }

    boost::asio::awaitable<const pair*> any_of(const keyset& keys) {
        while (true) {
            const pair* nf = find_any(keys);
            if (nf != nullptr) {
                co_return nf;
            }
            co_await notifier.async_receive(boost::asio::deferred);
        }
        co_return nullptr;
    }

};

using project_vec = std::vector<project_info>;

boost::asio::awaitable<void> run(const project_info& pi, project_completion& completion) {
    boost::asio::any_io_executor exec = co_await boost::asio::this_coro::executor;

    std::unordered_set<std::string> remaining_deps = pi.deps;


    while (!remaining_deps.empty()) {
        const project_completion::pair* p = co_await completion.any_of(remaining_deps);
        if (p == nullptr) {
            co_return;
        }

        pi.stream() << "project_completion " << p->first << ", " << static_cast<unsigned>(p->second) << std::endl;
        remaining_deps.erase(p->first);
    }

    pi.stream() << "building on thread id " << std::this_thread::get_id() << std::endl;
    try {
        int exit = co_await boost::process::v2::async_execute(
            boost::process::v2::process(exec, "test.zsh", {pi.name}), 
            boost::asio::deferred);

        completion.done(pi.name, exit == 0 ? project_status::SUCCESS : project_status::ERROR);
    } catch (std::exception& e) {
        pi.stream() << "unable to build " << pi.name << ": " << e.what() << std::endl;
        completion.done(pi.name, project_status::ERROR);
    }

}

void run_all(const project_vec& projects) {
    boost::asio::io_context ctx;

    project_completion completion(ctx);
    try {
        for (const project_info& pi: projects) {
            boost::asio::co_spawn(ctx, run(pi, completion), boost::asio::detached);
        }
        ctx.run();
    } catch (const std::exception& e) {
        std::cerr << "exception caught: " << e.what() << std::endl;
    }
}

int main() {
    project_vec projects = { 
        project_info { .name = "a", .deps = {} },
        project_info { .name = "b", .deps = {} },
        project_info { .name = "c", .deps = {"b"} },
        project_info { .name = "d", .deps = {"b"} },
        project_info { .name = "e", .deps = {"b","c"} },
        project_info { .name = "f", .deps = {"b","c"} }
    };
    run_all(projects);

    return EXIT_SUCCESS;
}

