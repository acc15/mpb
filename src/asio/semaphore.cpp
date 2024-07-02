#include "semaphore.hpp"

namespace mpb {

semaphore::guard::guard(semaphore& s): s(&s) {
}

semaphore::guard::guard(guard&& mv): s(mv.s) {
    mv.s = nullptr;
}

semaphore::guard::~guard() { 
    if (s != nullptr) {
        s->unlock();
    }
}

boost::asio::awaitable<void> semaphore::lock() {
    while (permits == 0) {
        co_await notifier.async_receive(boost::asio::deferred);
    }
    --permits;
}

void semaphore::unlock() {
    ++permits;
    notifier.try_send(boost::system::error_code{});
}

boost::asio::awaitable<semaphore::guard> semaphore::scoped_lock() {
    co_await lock();
    co_return guard(*this);
}

}