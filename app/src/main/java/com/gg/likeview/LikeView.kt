package com.gg.likeview

import android.animation.*
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import java.util.*
import android.support.v4.content.ContextCompat
import android.graphics.drawable.Drawable
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AccelerateDecelerateInterpolator


/**
 *  Create by GG on 2018/12/27
 *  mail is gg.jin.yu@gmail.com
 */
class LikeView : RelativeLayout {


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val ANIMATION_DURATION = 350L

    private val mRandom: Random by lazy { Random() }

    private val mImages: IntArray by lazy { intArrayOf(R.drawable.pl_blue, R.drawable.pl_red, R.drawable.pl_yellow) }

    private var mDrawableWidth: Int
    private var mDrawableHeight: Int

    private lateinit var animation: AnimatorSet

    private val mInterpolators: Array<Interpolator> by lazy {
        arrayOf<Interpolator>(
            AccelerateDecelerateInterpolator(),
            AccelerateInterpolator(),
            DecelerateInterpolator(),
            LinearInterpolator()
        )
    }

    init {
        val drawable = ContextCompat.getDrawable(context, R.drawable.pl_blue)
        mDrawableWidth = drawable!!.intrinsicWidth
        mDrawableHeight = drawable.intrinsicHeight
    }

    fun addHeart() {
        val heartView = ImageView(context)
        heartView.setImageResource(mImages[mRandom.nextInt(mImages.size - 1)])
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            addRule(RelativeLayout.CENTER_HORIZONTAL)
        }
        heartView.layoutParams = layoutParams
        addView(heartView)

        animation = getAllAnimation(heartView)

        animation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                removeView(heartView)
            }
        })

        animation.start()
    }

    private fun getAllAnimation(view: View): AnimatorSet {

        val animatorSet = AnimatorSet()
        val viewAnimation = AnimatorSet()
        viewAnimation.apply {
            playTogether(
                ObjectAnimator.ofFloat(view, "alpha", 0.3f, 1f),
                ObjectAnimator.ofFloat(view, "scaleX", 0.3f, 1f),
                ObjectAnimator.ofFloat(view, "scaleY", 0.3f, 1f)
            )
            duration = ANIMATION_DURATION
        }

        animatorSet.playSequentially(viewAnimation, getPathAnimation(view))

        return animatorSet
    }

    private fun getPathAnimation(view: View): Animator {


        val pointF0 = PointF((width - mDrawableWidth) / 2f, height - mDrawableHeight.toFloat())
        val pointF1 = getPoint(1)
        val pointF2 = getPoint(2)
        val pointF3 = PointF(mRandom.nextInt(width - mDrawableWidth).toFloat(), 0f)


        val likeTypeEvaluator = LikeTypeEvaluator(pointF1, pointF2)

        return ObjectAnimator.ofObject(likeTypeEvaluator, pointF0, pointF3).apply {
            duration = 3000
            interpolator = mInterpolators[mRandom.nextInt(mInterpolators.size - 1)]
            addUpdateListener {
                val pointF = it.animatedValue as PointF
                view.x = pointF.x
                view.y = pointF.y

                view.alpha = 1 - it.animatedFraction + 0.2f
            }
        }


    }

    private fun getPoint(index: Int): PointF { // 1
        return PointF(
            mRandom.nextInt(width) - mDrawableWidth.toFloat(),
            mRandom.nextInt(height / 2) + (index - 1) * (height) / 2f
        )
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (this::animation.isInitialized) {
            animation.end()
        }
        removeAllViews()
    }

    private class LikeTypeEvaluator(private var pointF1: PointF, private var pointF2: PointF) : TypeEvaluator<PointF> {


        override fun evaluate(t: Float, pointF0: PointF, pointF3: PointF): PointF {

            val pointF = PointF()

            pointF.x = (pointF0.x * (1 - t) * (1 - t) * (1 - t)
                    + 3f * pointF1.x * t * (1 - t) * (1 - t)
                    + 3f * pointF2.x * t * t * (1 - t)
                    + pointF3.x * t * t * t)


            pointF.y = (pointF0.y * (1 - t) * (1 - t) * (1 - t)
                    + 3f * pointF1.y * t * (1 - t) * (1 - t)
                    + 3f * pointF2.y * t * t * (1 - t)
                    + pointF3.y * t * t * t)



            return pointF
        }

    }
}