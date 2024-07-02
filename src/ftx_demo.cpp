// Copyright 2020 Arthur Sonzogni. All rights reserved.
// Use of this source code is governed by the MIT license that can be found in
// the LICENSE file.
#include <ftxui/dom/elements.hpp> // for Element, operator|, separator, filler, hbox, size, spinner, text, vbox, bold, border, Fit, EQUAL, WIDTH
#include <ftxui/dom/node.hpp>     // for Render
#include <ftxui/screen/screen.hpp> // for Full, Screen
#include <iostream>                // for cout, endl, ostream
#include <string>                  // for to_string, operator<<, string
#include <thread>                  // for sleep_for
#include <algorithm>

namespace ftxui { namespace addons {

class BgGauge : public ::ftxui::Node {

    Color start_;
    Color end_;
    float value_;

public:
    BgGauge(Element child, Color start, Color end, float value)
      : Node({ std::move(child) }), start_(start), end_(end), value_(std::clamp(value, 0.f, 1.f)) 
    {
    }

    void Render(Screen& screen) override {
        int min = box_.x_min, max = box_.x_max, w = max - min + 1, lb = min + static_cast<int>(value_ * w);
        for (int x = min; x < lb; ++x) {
            for (int y = box_.y_min; y <= box_.y_max; ++y) {
                screen.PixelAt(x, y).background_color = start_;
            }
        }
        for (int x = lb; x <= max; ++x) {
            for (int y = box_.y_min; y <= box_.y_max; ++y) {
                screen.PixelAt(x, y).background_color = end_;
            }
        }
        Node::Render(screen);
    }

    void ComputeRequirement() override {
        Node::ComputeRequirement();
        requirement_ = children_[0]->requirement();
    }

    void SetBox(Box box) override {
        Node::SetBox(box);
        children_[0]->SetBox(box);
    }
};

::ftxui::Decorator bgGauge(Color start, Color end, float value) {
    return [start, end, value](Element child) { 
        return std::make_shared<BgGauge>(std::move(child), start, end, value); 
    };
}

}}


int main() {

  using namespace ftxui;
  using namespace std::chrono_literals;

  std::string reset_position;
  for (int index = 0; index <= 120; ++index) {

    float v = static_cast<float>(index) / 120;
    auto document = hbox({ 
        text("prefix"), 
        text(" "), 
        text("hello " + std::to_string(index) + " of 120") 
            | color(Color::Black) 
            | ftxui::center 
            | ftxui::addons::bgGauge(Color::Green1, Color::BlueLight, v)
            | size(ftxui::WIDTH, ftxui::EQUAL, 50)
    });
    auto screen = Screen::Create(Dimension::Full(), Dimension::Fit(document));
    Render(screen, document);
    std::cout << reset_position;
    screen.Print();
    reset_position = screen.ResetPosition();

    std::this_thread::sleep_for(0.1s);
  }
  std::cout << std::endl;
}