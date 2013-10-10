/* -*- mode: c++; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
#ifndef _SAMPLE_H_
#define _SAMPLE_H_

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

#endif /* _SAMPLE_H_ */
