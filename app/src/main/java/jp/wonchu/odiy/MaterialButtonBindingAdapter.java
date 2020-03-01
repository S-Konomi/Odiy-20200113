package jp.wonchu.odiy;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.InverseBindingMethod;
import androidx.databinding.InverseBindingMethods;

import com.google.android.material.button.MaterialButton;

// データバインディングを使って、MaterialButtonに対してcheckedの制御ができない問題の回避のため、
// CompoundButtonのbinding adapterに実装されているcheckedまわりの処理をコピペ。
// 参考: https://android.googlesource.com/platform/frameworks/data-binding/+/master/extensions/baseAdapters/src/main/java/android/databinding/adapters/CompoundButtonBindingAdapter.java
@InverseBindingMethods({
        @InverseBindingMethod(type = MaterialButton.class, attribute = "android:checked"),
})
public class MaterialButtonBindingAdapter {
    @BindingAdapter("android:checked")
    public static void setChecked(MaterialButton view, boolean checked) {
        if (view.isChecked() != checked) {
            view.setChecked(checked);
        }
    }

    @BindingAdapter(value = {"android:checkedAttrChanged"}, requireAll = false)
    public static void setListeners(MaterialButton view, final InverseBindingListener attrChange) {
        if (attrChange != null) {
            // 丁寧に実装するには、TextViewのbinding adapterのようにListenerUtilというクラスを使って
            // 前回仕掛けたリスナーを解除する必要がある。
            // 参考: https://android.googlesource.com/platform/frameworks/data-binding/+/master/extensions/baseAdapters/src/main/java/android/databinding/adapters/TextViewBindingAdapter.java
            //
            // しかし、現状はそこまでは必要なく、ただただ面倒なだけなので、全部のリスナーを解除してしまう。
            view.clearOnCheckedChangeListeners();

            view.addOnCheckedChangeListener(new MaterialButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(MaterialButton buttonView, boolean isChecked) {
                    attrChange.onChange();
                }
            });
        }
    }
}
