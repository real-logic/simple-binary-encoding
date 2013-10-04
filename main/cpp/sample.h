#include <stdio.h>

class SomethingBasic
{
public:

    SomethingBasic(int val)
    {
        val_ = val;
    }

    int val(void)
    {
        return val_;
    }

private:
    int val_;
};
