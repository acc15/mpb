
#ifdef _WIN32
    #include <windows.h>
#else
    #include <sys/ioctl.h>
    #include <termios.h>
    #include <unistd.h>
#endif

#include <iostream>
#include <string>
#include <cassert>
#include <format>

const std::string SHOW_CURSOR = "\33[?25h";
const std::string HIDE_CURSOR = "\33[?25l";
const std::string RESET_ATTRS = "\33[0m";
const std::string GET_CURSOR_POS = "\33[6n";

enum move_mode {
    UP, DOWN, FORWARD, BACKWARD, NEXT_LINE, PREV_LINE, HORIZONTAL_ABSOLUTE
};

std::string move_cursor(move_mode mode, unsigned int value) {
    return std::format("\33[{}{}", value, static_cast<char>('A' + mode));
}

/*
CSI n A	CUU	Cursor Up	Moves the cursor n (default 1) cells in the given direction. If the cursor is already at the edge of the screen, this has no effect.
CSI n B	CUD	Cursor Down
CSI n C	CUF	Cursor Forward
CSI n D	CUB	Cursor Back
CSI n E	CNL	Cursor Next Line	Moves cursor to beginning of the line n (default 1) lines down. (not ANSI.SYS)
CSI n F	CPL	Cursor Previous Line	Moves cursor to beginning of the line n (default 1) lines up. (not ANSI.SYS)
CSI n G	CHA	Cursor Horizontal Absolute	Moves the cursor to column n (default 1). (not ANSI.SYS)
CSI n ; m H	CUP	Cursor Position	Moves the cursor to row n, column m. The values are 1-based, and default to 1 (top left corner) if omitted. A sequence such as CSI ;5H is a synonym for CSI 1;5H as well as CSI 17;H is the same as CSI 17H and CSI 17;1H
CSI n J	ED	Erase in Display	Clears part of the screen. If n is 0 (or missing), clear from cursor to end of screen. If n is 1, clear from cursor to beginning of the screen. If n is 2, clear entire screen (and moves cursor to upper left on DOS ANSI.SYS). If n is 3, clear entire screen and delete all lines saved in the scrollback buffer (this feature was added for xterm and is supported by other terminal applications).
CSI n K	EL	Erase in Line	Erases part of the line. If n is 0 (or missing), clear from cursor to the end of the line. If n is 1, clear from cursor to beginning of the line. If n is 2, clear entire line. Cursor position does not change.
CSI n S	SU	Scroll Up	Scroll whole page up by n (default 1) lines. New lines are added at the bottom. (not ANSI.SYS)
CSI n T	SD	Scroll Down	Scroll whole page down by n (default 1) lines. New lines are added at the top. (not ANSI.SYS)
*/

std::ostream& nl(std::ostream& s) {
    return s << '\n';
}

struct terminal_dimension {
    unsigned short rows;
    unsigned short cols;
};
std::ostream& operator<<(std::ostream& o, const terminal_dimension& v) {
    return o << v.cols << "x" << v.rows;
}

struct cursor_pos {
    unsigned short row;
    unsigned short col;
};
std::ostream& operator<<(std::ostream& o, const cursor_pos& v) {
    return o << v.col << "," << v.row;
}

terminal_dimension get_dimension() {
#ifdef _WIN32
    CONSOLE_SCREEN_BUFFER_INFO ws;
    GetConsoleScreenBufferInfo(GetStdHandle(STD_OUTPUT_HANDLE), &ws);
    return terminal_dimension { ws.srWindow.Bottom - ws.srWindow.Top + 1, ws.srWindow.Right - ws.srWindow.Left + 1 };
#else
    winsize ws;
    ioctl(STDOUT_FILENO, TIOCGWINSZ, &ws);
    return terminal_dimension { ws.ws_row, ws.ws_col };
#endif
}

cursor_pos get_cursor_pos() {
    std::cout << GET_CURSOR_POS << std::flush;
    unsigned int row, col;
    if (scanf("\33[%u;%uR", &row, &col) != 2) {
        throw std::runtime_error("can't parse cursor pos escape sequence");
    }
    return cursor_pos { static_cast<unsigned short>(row), static_cast<unsigned short>(col) };
}

void no_echo() {
    termios p;
    tcgetattr(STDOUT_FILENO, &p);
    p.c_lflag &= ~(ICANON | ECHO | ECHOE | ECHOK | ECHONL); 
    tcsetattr(STDOUT_FILENO, TCSANOW, &p);
}

int main() {

    if (!isatty(STDOUT_FILENO) || !isatty(STDIN_FILENO)) {
        std::cerr << "this program should run in terminal" << std::endl;
        return 1;
    }

    no_echo();

    auto p = get_cursor_pos();

    std::cout 
        << "hello noecho mode" << nl
        << "terminal size: " << get_dimension() << nl
        << "cursor pos: " << p << nl;

    int v;
    while((v = std::cin.get()) != 'y') {
        std::cout << move_cursor(UP, 1) << std::flush;
        // std::cout << (v == '\33' ? '^' : static_cast<char>(v));
    }
    std::cout << RESET_ATTRS << SHOW_CURSOR << std::flush;
    return 0;
}