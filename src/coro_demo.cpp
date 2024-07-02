#include <iostream>
#include <coroutine>
#include <map>
#include <utility>

using object_id = std::pair<const char*, unsigned int>;

std::ostream& operator<<(std::ostream& o, const object_id& i) {
    return o << i.first << ":" << i.second;
}

#define TRACE_CALL(msg) do { \
    std::cout << "[" << __FUNCTION__ << "] " \
        << msg << std::endl; \
    } while (false)

#define TRACE_CALL_ID(msg) TRACE_CALL("[" << id << "] " << msg)


object_id make_object_id(const char* class_name) {
    static std::map<const char*, unsigned int> id_map;
    return std::make_pair(class_name, ++id_map[class_name]);
}

template <typename Task, typename T>
struct mypromise {

    object_id id = make_object_id("promise");
    T result;

    Task get_return_object() { 
        TRACE_CALL_ID("");
        return { Task::from_promise(*this) }; 
    }
    
    std::suspend_always initial_suspend() noexcept { 
        TRACE_CALL_ID("");
        return {}; 
    }
    
    std::suspend_always final_suspend() noexcept { 
        TRACE_CALL_ID("");
        return {}; 
    }

    void return_value(int value) {
        TRACE_CALL_ID(value);
        result = value;
    }

    void unhandled_exception() {
        TRACE_CALL_ID("");
    }

};

struct task : std::coroutine_handle<mypromise<task, int>> {
    using promise_type = struct mypromise<task, int>;

    object_id id = make_object_id("task");

    bool await_ready() {
        TRACE_CALL_ID("");
        return false;
    }

    void await_suspend(std::coroutine_handle<promise_type> t) {
        TRACE_CALL_ID("suspending id = " << t.promise().id << ", result = " << t.promise().result);
        resume();
    }

     int await_resume() {
        TRACE_CALL_ID("promise.result = " << promise().result);
        return promise().result;
    }
};

task x() {
    TRACE_CALL("");
    co_return 1;
}

task y() {
    TRACE_CALL("");
    co_return 2;
}

task do_something_with_result() {
    TRACE_CALL("begin");
    int _x = co_await x();
    TRACE_CALL("after x = " << _x);
    int _y = co_await y();
    TRACE_CALL("after y = " << _y);
    co_return _x + _y;
}


void trace_coro(task(*coro)()) {
    auto c = coro();
    TRACE_CALL("coro create, id = " << c.id);

    while (!c.done()) {
        c.resume();
        TRACE_CALL("coro resume, id = " << c.id);
    }

    TRACE_CALL("coro return, id = " << c.id << ", result = " << c.promise().result);
    c.destroy();
    TRACE_CALL("coro destroy, id = " << c.id);
}

// struct call_tracer {
//     const char* fn_name;
//     std::ostream& out;

//     call_tracer(const char* fn_name, std::ostream& out = std::cout): fn_name(fn_name), out(out) {}
//     ~call_tracer() { out << std::endl; }
// };


int main() {
    trace_coro(do_something_with_result);
    return 0;
}