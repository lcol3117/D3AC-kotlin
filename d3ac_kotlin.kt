import com.sun.org.apache.xpath.internal.operations.Bool
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.*

fun main(args: Array<String>) {
    val cluster1: List<List<Int>> = listOf(listOf(1,2),listOf(3,4))
    val cluster2: List<List<Int>> = listOf(listOf(6,7),listOf(8,9))
    val mydata: List<List<Int>> = cluster1 + cluster2
    val mymodel: d3ac = d3ac(mydata,null)
    val myresults: List<List<List<Int>>> = mymodel.cluster(1)
    println(myresults)
}

class d3ac(val alldata: List<List<Int>>, var w: Int?) {
    fun cluster(w0: Int?): List<List<List<Int>>> {
        w = w0 ?: (w!!)
        val radii: List<Int> = alldata.map(::getDDRadius)
        val result = intersectionClustering(radii)
        return result
    }

    private fun getDDRadius(p: List<Int>): Int {
        var densities: MutableList<Double> = mutableListOf()
        var k: Int = 1
        var r: Int? = null
        var r1: Int? = null
        var ok: Boolean = true
        do {
            r = k * (w!!)
            r1 = (k - 1) * (w!!)
            densities.add(torusDensity(p, r1, r))
            ok = (if (densities.size < 2) true else checkDD(densities))
        } while (ok)
        return (r1!!)
    }

    private fun torusDensity(cp: List<Int>, low: Int, high: Int): Double {
        var cnt: Double = 0.0
        alldata.forEach { cnt += (if (inTorus(cp, low, high, it)) 1 else 0) }
        val a = (PI * high.toDouble().pow(2.0)) - (PI * low.toDouble().pow(2.0))
        val density: Double = cnt / a
        return density
    }

    private fun inTorus(cp: List<Int>, low: Int, high: Int, i: List<Int>): Boolean {
        val dist: Double = l2dist(cp, i)
        val inside1: Boolean = dist.toDouble() >= low.toDouble()
        val inside2: Boolean = dist.toDouble() <= high.toDouble()
        val inside: Boolean = inside1 and inside2
        return inside
    }

    private fun l2dist(p1: List<Int>, p2: List<Int>): Double {
        var sum: Double = 0.0
        p1.forEachIndexed { j, _ -> sum += (p1[j] - p2[j]).toDouble().pow(2.0) }
        val resultDist = sqrt(sum)
        return resultDist
    }

    private fun checkDD(given: MutableList<Double>): Boolean {
        val indexLast = given.size - 1
        val indexSndLast = given.size - 2
        val delta = given[indexLast] - given[indexSndLast]
        return (delta < 0)
    }

    private fun getMinWithin(p: List<Int>, r: Int): List<Int>? {
        var minWithin: List<Int>? = null
        for (i in alldata) {
            if (inTorus(p,0,r,i)) {
                if (lt2D(p.toList(),minWithin?:p.toList())) {
                    minWithin = p.toList()
                }
            }
        }
        return minWithin
    }

    private fun lt2D(a: List<Int>, b: List<Int>): Boolean {
        if (a == b) return false
        val ltbA: Int = abs(a[0] - b[0])
        val ltbB: Int = abs(a[1] - b[1])
        if (ltbA == ltbB) return a[0] < b[0]
        if (ltbA > ltbB) {
            return a[0] < b[0]
        } else {
            return a[1] < b[1]
        }
    }

    private fun intersectionClustering(radii: List<Int>): List<List<List<Int>>> {
        var cbps: MutableMap<List<Int>,MutableList<List<Int>>> = mutableMapOf()
        var pbo: List<Int>? = null
        var pb: List<Int>
        for (p in alldata) {
            pbo = getBaseClusterPoint(p,radii)
            pb = pbo!!
            if (pb !in cbps.keys) {
                cbps[pb] = mutableListOf(pb,p)
            } else {
                cbps[pb]!!.add(p)
            }
        }
        val clusters: List<List<List<Int>>> = cbps.values.distinct().toList()
        return clusters
    }

    private tailrec fun getBaseClusterPoint(p: List<Int>, r: List<Int>): List<Int>? {
        val useR: Int = r[alldata.indexOf(p)]
        val currentBaseClusterPoint: List<Int>? = getMinWithin(p,useR)
        return if (currentBaseClusterPoint == null) {
            p
        } else {
            getBaseClusterPoint(currentBaseClusterPoint,r)
        }
    }
}
