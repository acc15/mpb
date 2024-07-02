#pragma once

#include <boost/asio.hpp>
#include <boost/asio/experimental/channel.hpp>

namespace mpb {

class semaphore {
    
    using channel = boost::asio::experimental::channel<void(boost::system::error_code)>;

    channel notifier;
    unsigned int permits;

public:

    class guard {
        semaphore* s;
        guard(semaphore& s);
        friend semaphore;
    public:
        guard(guard&& mv);
        ~guard();
    };

    semaphore(boost::asio::io_context& ctx, unsigned int permits): notifier(ctx), permits(permits) {}

    boost::asio::awaitable<void> lock();
    void unlock();

    boost::asio::awaitable<guard> scoped_lock();

};


}