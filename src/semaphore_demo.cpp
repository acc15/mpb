#include <iostream>

#include <boost/asio.hpp>
#include <boost/asio/experimental/channel.hpp>

#include "asio/semaphore.hpp"

boost::asio::awaitable<void> run(unsigned int i, mpb::semaphore& sem) {
    auto executor = co_await boost::asio::this_coro::executor;
    
    //std::cout << "locking " << i << std::endl;
    {
        mpb::semaphore::guard g = co_await sem.scoped_lock();
        std::cout << "processing " << i << std::endl;
        co_await boost::asio::steady_timer(executor, std::chrono::seconds(1)).async_wait(boost::asio::deferred);
    }
    //std::cout << "after unlock " << i << std::endl;

}


int main() {
    boost::asio::io_context ctx;

    mpb::semaphore sem(ctx, 5);
    try {
        for (unsigned int i = 0 ; i < 100; i++) {
            boost::asio::co_spawn(ctx, run(i, sem), boost::asio::detached);
        }
        ctx.run();
    } catch (const std::exception& e) {
        std::cerr << "exception caught: " << e.what() << std::endl;
    }
}

