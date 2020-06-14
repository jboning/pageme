package name.jboning.pageme.annoyer

interface Annoyer {
    fun isNoisy(): Boolean
    fun start()
    fun stop()
}