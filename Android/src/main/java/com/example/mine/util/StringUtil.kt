package com.example.mine.util;

class StringUtil {

    /**
     * 提取字符串中第一个 [] 之间的内容
     *
     * @param input 输入字符串
     * @return 提取的内容，如果没有找到则返回空字符串
     */
    fun extractContentBetweenBrackets(input: String): String {
        if (input.isEmpty()) {
            return "" // 如果输入为空，直接返回空字符串
        }

        // 查找第一个 '[' 的位置
        val startIndex = input.indexOf('[')
        if (startIndex == -1) {
            return "" // 如果没有找到 '[', 返回空字符串
        }

        // 查找第一个 ']' 的位置
        val endIndex = input.indexOf(']', startIndex)
        if (endIndex == -1) {
            return "" // 如果没有找到 ']', 返回空字符串
        }

        // 提取 '[' 和 ']' 之间的内容
        return input.substring(startIndex + 1, endIndex)
    }
}
