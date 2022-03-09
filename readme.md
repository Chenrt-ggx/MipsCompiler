# Yu Ge Compiler

[![Build and Release](https://github.com/Chenrt-ggx/MipsCompiler/actions/workflows/build.yml/badge.svg)](https://github.com/Chenrt-ggx/MipsCompiler/actions/workflows/build.yml)

- Chenrt SysY Compiler (CSC)
- A Tiny Compiler Written In Java-8
- Translates SysY Language Into MIPS Assembly

## 基础功能
- [x] 词法分析
- [x] 语法分析
- [x] 中间代码
- [x] 代码生成

## 前端优化
- [x] 小型函数内联
- [x] 表达式翻译优化
- [x] 局部常数数组全局化
- [x] 循环翻译优化(默认开启)
- [x] 数组寻址运算强度削弱(默认开启)

## 中端优化
- [x] 常量传播
- [x] 复写传播
- [x] 死代码删除
- [x] 分支语句窥孔优化
- [x] 全局变量初始化优化

## 后端优化
- [x] 立即数乘除法优化
- [x] OPT临时寄存器分配
- [x] 图着色全局寄存器分配
- [x] 后端无用跳转语句删除
- [x] 后端无用赋值语句删除

## 后续工作
- [ ] 循环优化
- [ ] 尾递归转循环
- [ ] 公共子表达式删除
- [ ] 函数传参寄存器优化
- [ ] 全局变量寄存器优化
