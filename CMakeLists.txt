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

include(FetchContent)

FetchContent_Declare(ftxui
  GIT_REPOSITORY https://github.com/ArthurSonzogni/ftxui
  GIT_TAG v5.0.0
)

FetchContent_GetProperties(ftxui)
if(NOT ftxui_POPULATED)
  FetchContent_Populate(ftxui)
  add_subdirectory(${ftxui_SOURCE_DIR} ${ftxui_BINARY_DIR} EXCLUDE_FROM_ALL)
endif()

add_executable(ftx_demo 
    ${SRC}/ftx_demo.cpp
)
target_link_libraries(ftx_demo 
    PRIVATE ftxui::screen
    PRIVATE ftxui::dom 
    PRIVATE ftxui::component)

# testing(TARGET mpb SRC ...)