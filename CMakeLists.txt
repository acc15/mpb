cmake_minimum_required(VERSION 3.29.3)
project(mpb VERSION 0.1.0 LANGUAGES CXX)
include(cmake/helpers.cmake)
configure_defaults()

find_package(Boost REQUIRED)

add_definitions(-DBOOST_ASIO_CONCURRENCY_HINT_DEFAULT=1)

add_executable(coro_demo ${SRC}/coro_demo.cpp)
add_executable(regex_demo ${SRC}/regex_demo.cpp)
add_executable(fork_demo ${SRC}/fork_demo.cpp)
target_link_libraries(fork_demo Boost::boost)

add_executable(semaphore_demo 
    ${SRC}/asio/semaphore.cpp
    ${SRC}/asio/semaphore.hpp
    ${SRC}/semaphore_demo.cpp
)
target_link_libraries(semaphore_demo Boost::boost)
# testing(TARGET mpb SRC ...)