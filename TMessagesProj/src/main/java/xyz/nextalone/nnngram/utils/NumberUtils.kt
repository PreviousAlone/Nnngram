package xyz.nextalone.nnngram.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException

object NumberUtils {
    /**
     * 判断String是否是整数<br></br>
     * 支持10进制
     *
     * @param str String
     * @return 是否为整数
     */
    @JvmStatic
    fun isInteger(str: String): Boolean {
        try {
            str.toInt()
        } catch (e: NumberFormatException) {
            return false
        }
        return true
    }

    /**
     * 解析转换数字字符串为int型数字，规则如下：
     *
     * <pre>
     * 1、0x开头的视为16进制数字
     * 2、0开头的忽略开头的0
     * 3、其它情况按照10进制转换
     * 4、空串返回0
     * 5、.123形式返回0（按照小于0的小数对待）
     * 6、123.56截取小数点之前的数字，忽略小数部分
    </pre> *
     *
     * @param number 数字，支持0x开头、0开头和普通十进制
     * @return int
     * @throws NumberFormatException 数字格式异常
     * @since 4.1.4
     */
    @JvmStatic
    @Throws(NumberFormatException::class)
    fun parseInt(number: String): Int {
        return try {
            number.toInt()
        } catch (e: NumberFormatException) {
            parseNumber(number).toInt()
        }
    }

    /**
     * 将指定字符串转换为[Number] 对象<br></br>
     * 此方法不支持科学计数法
     *
     * @param numberStr Number字符串
     * @return Number对象
     * @throws NumberFormatException 包装了[ParseException]，当给定的数字字符串无法解析时抛出
     * @since 4.1.15
     */
    @Throws(NumberFormatException::class)
    fun parseNumber(numberStr: String?): Number {
        return try {
            val format = NumberFormat.getInstance()
            if (format is DecimalFormat) {
                // issue#1818@Github
                // 当字符串数字超出double的长度时，会导致截断，此处使用BigDecimal接收
                format.isParseBigDecimal = true
            }
            format.parse(numberStr)
        } catch (e: ParseException) {
            val nfe = NumberFormatException(e.message)
            nfe.initCause(e)
            throw nfe
        }
    }
}
