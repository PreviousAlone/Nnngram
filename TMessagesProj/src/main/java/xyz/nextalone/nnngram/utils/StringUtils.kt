package xyz.nextalone.nnngram.utils

object StringUtils {
    /**
     *
     * 字符串是否为空白，空白的定义如下：
     *
     *  1. `null`
     *  1. 空字符串：`""`
     *  1. 空格、全角空格、制表符、换行符，等不可见字符
     *
     *
     *
     * 例：
     *
     *  * `StringUtils.isBlank(null)     // true`
     *  * `StringUtils.isBlank("")       // true`
     *  * `StringUtils.isBlank(" \t\n")  // true`
     *  * `StringUtils.isBlank("abc")    // false`
     *
     *
     * @param str 被检测的字符串
     * @return 若为空白，则返回 true
     */
    @JvmStatic
    fun isBlank(str: CharSequence?): Boolean {
        var length: Int = 0
        if (str == null || str.length.also { length = it } == 0) {
            return true
        }
        for (i in 0 until length) {
            // 只要有一个非空字符即为非空字符串
            if (!isBlankChar(str[i])) {
                return false
            }
        }
        return true
    }

    /**
     * 是否空白符<br></br>
     * 空白符包括空格、制表符、全角空格和不间断空格<br></br>
     *
     * @param c 字符
     * @return 是否空白符
     * @see Character.isWhitespace
     * @see Character.isSpaceChar
     */
    fun isBlankChar(c: Int): Boolean {
        return Character.isWhitespace(c) || Character.isSpaceChar(c) || c == '\ufeff'.code || c == '\u202a'.code || c == '\u0000'.code
    }

    /**
     * 是否空白符<br></br>
     * 空白符包括空格、制表符、全角空格和不间断空格<br></br>
     *
     * @param c 字符
     * @return 是否空白符
     * @see Character.isWhitespace
     * @see Character.isSpaceChar
     */
    fun isBlankChar(c: Char): Boolean {
        return isBlankChar(c.code)
    }
}
