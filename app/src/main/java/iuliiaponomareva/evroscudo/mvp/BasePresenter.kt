package iuliiaponomareva.evroscudo.mvp


interface BasePresenter<V : BaseView> {
    fun attachView(view: V)
    fun detachView()
}


