//
// Created by 梁赛飞 on 16/12/26.
//

#ifndef VCBROWSERINTERNATIONAL_SYSTEMUTIL_H
#define VCBROWSERINTERNATIONAL_SYSTEMUTIL_H

#include "jni.h"
#include <malloc.h>

class Util {

    JNIEnv * env;




public:
    Util(JNIEnv *env) : env(env) { }
    ~Util(){
    }
    void forceExit();
};


#endif //VCBROWSERINTERNATIONAL_SYSTEMUTIL_H
