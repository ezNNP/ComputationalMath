import kotlin.math.pow
import kotlin.math.sqrt

class GaussSeidel(private var eps: Double) {

    fun solve(): Array<Int> {
        val size = 5
        var input: Array<Array<Double>> = Array(size){ Array(size) {0.0} }
        var previous: Array<Double> = Array(size){0.0}
        var current: Array<Double> = Array(size){0.0}
        do {
            for (i in current.indices) {
                previous[i] = current[i]
            }
            for (i in current.indices) {
                var new_coef: Double = 0.0
                for (j in 0 until i) {
                    new_coef += input[i][j] * current[j]
                }
                for (j in (i+1) until size) {
                    new_coef += input[i][j] * previous[j];
                }
                current[i] = ()
            }
        } while (!converge())
    }

    private fun converge(current: Array<Double>, previous: Array<Double>): Boolean {
        var norm: Double = 0.0;
        for (i in current.indices) {
            norm += (current[i] - previous[i]).pow(2.0);
        }
        return (sqrt(norm) < eps);
    }
}