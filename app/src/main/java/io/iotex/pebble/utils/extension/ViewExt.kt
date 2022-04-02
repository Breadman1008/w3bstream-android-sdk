package io.iotex.pebble.utils.extension

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.blankj.utilcode.util.SpanUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.gif.GifDrawable
import org.jetbrains.anko.attempt


fun View.setGone() {
    visibility = View.GONE
}

fun View.setVisible() {
    visibility = View.VISIBLE
}

fun View.setInvisible() {
    visibility = View.INVISIBLE
}

fun TextView.renderHighlightTips(source: String, highlight: String) {
    val tipsList = source.split(Regex("((?<=$highlight)|(?=$highlight))"))

    val spanUtils = SpanUtils.with(this)

    tipsList.forEach {
        if (it == highlight) {
            spanUtils.setTitleHighlightStyle(it)
        } else {
            spanUtils.setTitleNormalStyle(it)
        }
    }

    spanUtils.create()
}


fun ImageView.bitmapRequestBuilder(): RequestBuilder<Bitmap> {
    return Glide.with(this)
            .asBitmap()
}

fun ImageView.gifRequestBuilder(): RequestBuilder<GifDrawable> {
    return Glide.with(this)
            .asGif()
}

fun ImageView.loadGif(model: Any?, @DrawableRes holder: Int) {
    attempt {
        gifRequestBuilder()
                .load(model)
                .placeholder(holder)
                .into(this)
    }
}

fun ImageView.loadImage(model: Any?, @DrawableRes holder: Int) {
    attempt {
        bitmapRequestBuilder()
                .load(model)
                .thumbnail(bitmapRequestBuilder().load(holder))
                .error(bitmapRequestBuilder().load(holder))
                .into(this)
    }
}

fun ImageView.loadRoundImage(model: Any?, @DrawableRes holder: Int) {
    attempt {
        bitmapRequestBuilder()
                .load(model)
                .thumbnail(bitmapRequestBuilder().load(holder))
                .error(bitmapRequestBuilder().load(holder))
                .transform(CircleCrop())
                .into(this)
    }
}

fun ImageView.loadImage(model: Any?, @DrawableRes holder: Int, vararg transformation: Transformation<Bitmap>) {
    attempt {
        bitmapRequestBuilder()
                .load(model)
                .thumbnail(bitmapRequestBuilder().load(holder).transform(*transformation))
                .error(bitmapRequestBuilder().load(holder).transform(*transformation))
                .transform(*transformation)
                .into(this)
    }
}