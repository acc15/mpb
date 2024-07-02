#include <iostream>
#include <string>
#include <cwchar>
#include <regex>

size_t char_count(const std::string& str) {
    std::mbstate_t s = std::mbstate_t();
    const char* c_str = str.c_str();
    return mbsrtowcs(nullptr, &c_str, str.length(), &s);
}

int main() {
    std::locale::global(std::locale(""));

    const std::string str = "Привет, мир!!!";
    std::cout << str << std::endl;
    std::cout << "count of bytes: " << str.length() << ", count of chars: " << char_count(str) << std::endl;
    
    std::regex re("([А-Яа-я]+) (\\d+) (\\w+)");
    
    std::cmatch m;
    if (std::regex_match("Привет 17 years", m, re)) {
        std::cout << "regex matches!!!" << std::endl;
        for (const auto& sm: m) {
            std::cout << "group: " << sm.str() << std::endl;
        }
    }
    return 0;
}