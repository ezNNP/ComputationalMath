package utils

import model.Formula
import model.Point
import net.objecthunter.exp4j.ExpressionBuilder
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.ln
import kotlin.math.pow

class ApproximationMethod {

    private val decimalFormatSymbols = DecimalFormatSymbols(Locale.GERMAN)
    lateinit var format: DecimalFormat

    enum class Type {
        LINEAR,
        SQUARE,
        CUBE,
        POWER,
        HYPERBOLA,
        INDICATIVE,
        LOG,
        EXP
    }

    fun findBest(data: Array<Point>): Formula {
        var bestFormula: Formula? = null
        var minDelta = Double.MAX_VALUE
        enumValues<Type>().forEach next@{
            val formula = try {
                evaluate(data, it)
            } catch (e: Exception) {
                return@next
            }
            var delta = 0.0
            data.forEach {point ->
                delta += (point.y - formula.expression.setVariable("x", point.x).evaluate()).pow(2)
            }
            if (delta < minDelta) {
                minDelta = delta
                bestFormula = formula
            }
        }
        if (bestFormula != null) {
            return bestFormula as Formula
        } else {
            throw Exception()
        }
    }

    fun evaluate(data: Array<Point>, type: Type, size: Int = data.size): Formula {
        decimalFormatSymbols.decimalSeparator = '.'
        format = DecimalFormat("#.#####", decimalFormatSymbols)
        format.roundingMode = RoundingMode.CEILING
        return when (type) {
            Type.LINEAR -> linear(size, data)
            Type.SQUARE -> square(size, data)
            Type.CUBE -> cube(size, data)
            Type.POWER -> power(size, data)
            Type.HYPERBOLA -> hyperbola(size, data)
            Type.INDICATIVE -> indicative(size, data)
            Type.LOG -> log(size, data)
            Type.EXP -> exp(size, data)
        }
    }

    private fun linear(size: Int, data: Array<Point>): Formula {
        var sumx = 0.0
        var sumy = 0.0
        var sumxy = 0.0
        var sumx2 = 0.0
        for (i in data.indices) {
            sumx += data[i].x
            sumy += data[i].y
            sumxy += data[i].x * data[i].y
            sumx2 += data[i].x.pow(2)
        }
        val a: Double = format.format((sumx*sumy - size*sumxy) / (sumx.pow(2) - size * sumx2)).toDouble()
        val b: Double = format.format((sumx*sumxy - sumx2*sumy) / (sumx.pow(2) - size * sumx2)).toDouble()
        if (a.isFinite() && b.isFinite()) {
            val formulaString = if (b >= 0) {
                "$a*x+$b"
            } else {
                "$a*x$b"
            }
            val expression = ExpressionBuilder(formulaString).variable("x").build()
            return Formula(formulaString, expression)
        } else {
            throw Exception()
        }
    }

    private fun square(size: Int, data: Array<Point>): Formula {
        var sumx = 0.0
        var sumy = 0.0
        var sumx2 = 0.0
        var sumx3 = 0.0
        var sumx4 = 0.0
        var sumxy = 0.0
        var sumx2y = 0.0
        for (i in data.indices) {
            val x = data[i].x
            val y = data[i].y
            sumx += x
            sumy += y
            sumx2 += x.pow(2)
            sumx3 += x.pow(3)
            sumx4 += x.pow(4)
            sumxy += x*y
            sumx2y += x.pow(2)*y
        }

        val matrix = Array(3) {DoubleArray(3)}
        matrix[0][0] = sumx2
        matrix[0][1] = sumx
        matrix[0][2] = size*1.0
        matrix[1][0] = sumx3
        matrix[1][1] = sumx2
        matrix[1][2] = sumx
        matrix[2][0] = sumx4
        matrix[2][1] = sumx3
        matrix[2][2] = sumx2
        val d = determinant(matrix)

        matrix[0][0] = sumy
        matrix[1][0] = sumxy
        matrix[2][0] = sumx2y
        val da = determinant(matrix)

        matrix[0][0] = sumx2
        matrix[1][0] = sumx3
        matrix[2][0] = sumx4
        matrix[0][1] = sumy
        matrix[1][1] = sumxy
        matrix[2][1] = sumx2y
        val db = determinant(matrix)

        matrix[0][1] = sumx
        matrix[1][1] = sumx2
        matrix[2][1] = sumx3
        matrix[0][2] = sumy
        matrix[1][2] = sumxy
        matrix[2][2] = sumx2y
        val dc = determinant(matrix)
        val a = format.format(da/d).toDouble()
        val b = format.format(db/d).toDouble()
        val c = format.format(dc/d).toDouble()
        if (a.isFinite() && b.isFinite() && c.isFinite()) {
            var formulaString = "$a*x^2"
            formulaString += if (b >= 0) {
                "+$b*x"
            } else {
                "$b*x"
            }
            formulaString += if (c >= 0) {
                "+$c"
            } else {
                "$c"
            }
            val expression = ExpressionBuilder(formulaString).variable("x").build()
            return Formula(formulaString, expression)
        } else {
            throw Exception()
        }
    }

    private fun cube(size: Int, data: Array<Point>): Formula {
        var sumx = 0.0
        var sumx2 = 0.0
        var sumx3 = 0.0
        var sumx4 = 0.0
        var sumx5 = 0.0
        var sumx6 = 0.0
        var sumy = 0.0
        var sumxy = 0.0
        var sumx2y = 0.0
        var sumx3y = 0.0
        for (i in data.indices) {
            val x = data[i].x
            val y = data[i].y
            sumx += x
            sumx2 += x.pow(2)
            sumx3 += x.pow(3)
            sumx4 += x.pow(4)
            sumx5 += x.pow(5)
            sumx6 += x.pow(6)
            sumy += y
            sumxy += x * y
            sumx2y += x.pow(2) * y
            sumx3y += x.pow(3) * y
        }

        val matrix = Array(4) {DoubleArray(4)}
        matrix[0][0] = sumx3
        matrix[0][1] = sumx2
        matrix[0][2] = sumx
        matrix[0][3] = size * 1.0
        matrix[1][0] = sumx4
        matrix[1][1] = sumx3
        matrix[1][2] = sumx2
        matrix[1][3] = sumx
        matrix[2][0] = sumx5
        matrix[2][1] = sumx4
        matrix[2][2] = sumx3
        matrix[2][3] = sumx2
        matrix[3][0] = sumx6
        matrix[3][1] = sumx5
        matrix[3][2] = sumx4
        matrix[3][3] = sumx3
        val d = determinant(matrix)

        matrix[0][0] = sumy
        matrix[1][0] = sumxy
        matrix[2][0] = sumx2y
        matrix[3][0] = sumx3y
        val da = determinant(matrix)

        matrix[0][0] = sumx3
        matrix[1][0] = sumx4
        matrix[2][0] = sumx5
        matrix[3][0] = sumx6
        matrix[0][1] = sumy
        matrix[1][1] = sumxy
        matrix[2][1] = sumx2y
        matrix[3][1] = sumx3y
        val db = determinant(matrix)

        matrix[0][1] = sumx2
        matrix[1][1] = sumx3
        matrix[2][1] = sumx4
        matrix[3][1] = sumx5
        matrix[0][2] = sumy
        matrix[1][2] = sumxy
        matrix[2][2] = sumx2y
        matrix[3][2] = sumx3y
        val dc = determinant(matrix)

        matrix[0][2] = sumx
        matrix[1][2] = sumx2
        matrix[2][2] = sumx3
        matrix[3][2] = sumx4
        matrix[0][3] = sumy
        matrix[1][3] = sumxy
        matrix[2][3] = sumx2y
        matrix[3][3] = sumx3y
        val dd = determinant(matrix)

        val pa = format.format(da/d).toDouble()
        val pb = format.format(db/d).toDouble()
        val pc = format.format(dc/d).toDouble()
        val pd = format.format(dd/d).toDouble()

        if (pa.isFinite() && pb.isFinite() && pc.isFinite() && pd.isFinite()) {
            var formulaString = "$pa * x^3"
            formulaString += if (pb >= 0) {
                "+$pb*x^2"
            } else {
                "$pb*x^2"
            }
            formulaString += if (pc >= 0) {
                "+$pc*x"
            } else {
                "$pc*x"
            }
            formulaString += if (pd >= 0) {
                "+$pd"
            } else {
                "$pd"
            }
            val expression = ExpressionBuilder(formulaString).variable("x").build()
            return Formula(formulaString, expression)
        } else {
            throw Exception()
        }
    }

    private fun power(size: Int, data: Array<Point>): Formula {
        var sumx = 0.0
        var sumx2 = 0.0
        var sumy = 0.0
        var sumxy = 0.0
        for (i in data.indices) {
            val x = data[i].x
            val y = data[i].y
            sumx += ln(x)
            sumy += ln(y)
            sumx2 += ln(x).pow(2)
            sumxy += ln(x) * ln(y)
        }
        val b = format.format((size * sumxy - sumx * sumy) / (size * sumx2 - sumx.pow(2))).toDouble()
        val a = format.format(kotlin.math.exp(1.0/size * sumy - b/size * sumx)).toDouble()
        if (a.isFinite() && b.isFinite()) {
            val formulaString = "$a * x ^ $b"
            val expression = ExpressionBuilder(formulaString).variable("x").build()
            return Formula(formulaString, expression)
        } else {
            throw Exception()
        }
    }

    private fun hyperbola(size: Int, data: Array<Point>): Formula {
        var sumx = 0.0
        var sumx2 = 0.0
        var sumyx = 0.0
        var sumy = 0.0
        for (i in data.indices) {
            val x = data[i].x
            val y = data[i].y
            sumx += 1/x
            sumx2 += 1/ x.pow(2)
            sumyx += y/x
            sumy += y
        }

        val b = format.format((size * sumyx - sumx * sumy) / (size * sumx2 - sumx.pow(2))).toDouble()
        val a = format.format(1.0/size * sumy - b/size * sumx).toDouble()
        if (a.isFinite() && b.isFinite()) {
            val formulaString = if (b > 0) {
                "$a + $b/x"
            } else {
                "$a $b/x"
            }
            val expression = ExpressionBuilder(formulaString).variable("x").build()
            return Formula(formulaString, expression)
        } else {
            throw Exception()
        }
    }

    private fun indicative(size: Int, data: Array<Point>): Formula {
        var sumx = 0.0
        var sumx2 = 0.0
        var sumlny = 0.0
        var sumxlny = 0.0
        for (i in data.indices) {
            val x = data[i].x
            val y = data[i].y
            sumx += x
            sumx2 += x.pow(2)
            sumlny += ln(y)
            sumxlny += x * ln(y)
        }

        val b = format.format(kotlin.math.exp((size * sumxlny - sumx * sumlny) / (size * sumx2 - sumx.pow(2)))).toDouble()
        val a = format.format(kotlin.math.exp(1.0/size * sumlny - ln(b)/size * sumx)).toDouble()

        if (a.isFinite() && b.isFinite()) {
            val formulaString = "$a * $b ^ x"
            val expression = ExpressionBuilder(formulaString).variable("x").build()
            return Formula(formulaString, expression)
        } else {
            throw Exception()
        }
    }

    private fun log(size: Int, data: Array<Point>): Formula {
        var sumylnx = 0.0
        var sumlnx = 0.0
        var sumy = 0.0
        var sumln2x = 0.0
        for (i in data.indices) {
            val x = data[i].x
            val y = data[i].x
            sumylnx += y * ln(x)
            sumlnx += ln(x)
            sumy += y
            sumln2x += ln(x).pow(2)
        }

        val b = format.format((size * sumylnx - sumlnx * sumy) / (size * sumln2x - sumlnx.pow(2))).toDouble()
        val a = format.format(1.0/size * sumy - b/size * sumlnx).toDouble()

        if (a.isFinite() && b.isFinite()) {
            val formulaString = if (b >= 0) {
                "$a+$b * log(x)"
            } else {
                "$a$b * log(x)"
            }
            val expression = ExpressionBuilder(formulaString).variable("x").build()
            return Formula(formulaString, expression)
        } else {
            throw Exception()
        }
    }

    private fun exp(size: Int, data: Array<Point>): Formula {
        var sumxlny = 0.0
        var sumx = 0.0
        var sumlny = 0.0
        var sumx2 = 0.0

        for (i in data.indices) {
            val x = data[i].x
            val y = data[i].y
            sumxlny += x * ln(y)
            sumx += x
            sumlny += ln(y)
            sumx2 += x.pow(2)
        }

        val b = format.format((size * sumxlny - sumx * sumlny) / (size * sumx2 - sumx.pow(2))).toDouble()
        val a = format.format(1.0/size * sumlny - b/size * sumx).toDouble()
        if (a.isFinite() && b.isFinite()) {
            val formulaString = if (b >= 0) {
                "e^($a+$b * x)"
            } else {
                "e^($a$b * x)"
            }
            val expression = ExpressionBuilder(formulaString).variable("x").build()
            return Formula(formulaString, expression)
        } else {
            throw Exception()
        }
    }

    private fun determinant(a: Array<DoubleArray>): Double {
        if (a.size == 1) {
            return a[0][0]
        }
        if (a.size == 2) {
            return a[0][0] * a[1][1] - a[0][1] * a[1][0]
        }
        var sum = 0.0
        for (i in a.indices) {
            val subArray = Array(a.size - 1) {DoubleArray(a.size - 1)}
            var offsetColumn = 0
            val sign = -1.0
            for (row in 1 until a.size) {
                for (column in 0 until a.size - 1) {
                    if (column == i) {
                        offsetColumn = 1
                    }
                    subArray[row - 1][column] = a[row][column + offsetColumn]
                }
                offsetColumn = 0
            }
            sum += sign.pow(i) * a[0][i] * determinant(subArray)
        }
        return sum
    }
}