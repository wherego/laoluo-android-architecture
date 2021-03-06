package cn.mark.network.Task;

import android.util.Log;

import cn.mark.network.retrofit.bean.InfoBean;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yaoping on 2016/5/26.
 */

public abstract class RxJavaNet<T extends InfoBean> extends Subscriber<T> {
    protected Observable<T> srvObservable;

    @Override
    public void onCompleted() {
        Log.i("retrofit", "onCompleted");
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof RxError) {//判断错误是否为网络请求发送的错误
            Log.i("retrofit", "onError=" + ((RxError) e).getError_messag() + ",about infoBean");
        } else {
            Log.i("retrofit", "onError=" + e.getMessage());
        }
    }

    @Override
    abstract public void onNext(T data);

    /**
     * 在后台线程处理成功获取的数据
     */
    public void onNextBackgroundSuccess(T data) {

    }

    public void execute() {
        srvObservable.subscribeOn(Schedulers.io())//程序执行的线程
                .flatMap(new Func1<T, Observable<T>>() {
                    @Override
                    public Observable<T> call(T t) {
                        if (0 != t.error_code) //判断返回的结果是否请求成功
                            return Observable.error(new RxError(t));

                        onNextBackgroundSuccess(t);
                        return Observable.just(t);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//回到主线程
                .subscribe(this);
    }
}
