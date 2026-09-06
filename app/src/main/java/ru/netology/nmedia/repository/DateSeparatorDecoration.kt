package ru.netology.nmedia.repository

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.view.View
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.adapter.PostsAdapter
import java.time.Instant
import java.time.temporal.ChronoUnit

class DateSeparatorDecoration(
    private val postsAdapter: PostsAdapter,
    private val concatAdapter: ConcatAdapter,
) : RecyclerView.ItemDecoration() {

    private fun headerSize(): Int {
        val adapters = concatAdapter.adapters
        val index = adapters.indexOf(postsAdapter)
        if (index < 0) return 0
        var count = 0
        for (i in 0 until index) {
            count += adapters[i].itemCount
        }
        return count
    }

    private fun dataPosition(position: Int): Int = position - headerSize()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        try {
            val position = parent.getChildAdapterPosition(view)
            if (position < 0) return
            val dataPos = dataPosition(position)
            if (dataPos < 0 || dataPos >= postsAdapter.itemCount) return

            val post = postsAdapter.getItemAt(dataPos) ?: return
            val publishedAt = try {
                parseInstant(post.published)
            } catch (e: Exception) {
                return
            }

            if (shouldDrawSeparatorBefore(dataPos, publishedAt)) {
                val density = view.context.resources.displayMetrics.density
                outRect.top = (56f * density).toInt()
            }
        } catch (e: Exception) {
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        try {
            val layoutManager = parent.layoutManager as? LinearLayoutManager ?: return

            val start = layoutManager.findFirstVisibleItemPosition()
            val end = layoutManager.findLastVisibleItemPosition() + 1
            if (start < 0 || end <= start) return

            for (position in start until end) {
                if (position < 0 || position >= parent.adapter?.itemCount ?: 0) continue

                val view = parent.getChildAt(position - start) ?: continue
                val dataPos = dataPosition(position)
                if (dataPos < 0 || dataPos >= postsAdapter.itemCount) continue

                val post = postsAdapter.getItemAt(dataPos) ?: continue
                val publishedAt = try {
                    parseInstant(post.published)
                } catch (e: Exception) {
                    continue
                }

                if (!shouldDrawSeparatorBefore(dataPos, publishedAt)) continue

                val label = getLabel(publishedAt)
                drawSeparator(c, parent, view, label)
            }
        } catch (e: Exception) {
        }
    }

    private fun shouldDrawSeparatorBefore(
        currentPosition: Int,
        publishedAt: Instant,
    ): Boolean {
        if (currentPosition == 0) return true

        val prevPosition = currentPosition - 1
        if (prevPosition < 0) return true

        val prevItem = postsAdapter.getItemAt(prevPosition) ?: return true
        val prevPublishedAt = try {
            parseInstant(prevItem.published)
        } catch (e: Exception) {
            return true
        }

        return getGroupKey(publishedAt) != getGroupKey(prevPublishedAt)
    }

    private fun getGroupKey(instant: Instant): String {
        val now = Instant.now()
        val todayThreshold = now.minus(24, ChronoUnit.HOURS)
        val yesterdayThreshold = now.minus(48, ChronoUnit.HOURS)

        return when {
            instant.isAfter(todayThreshold) || instant == todayThreshold -> "TODAY"
            instant.isAfter(yesterdayThreshold) || instant == yesterdayThreshold -> "YESTERDAY"
            else -> "OLDER"
        }
    }

    private fun getLabel(instant: Instant): String = when (getGroupKey(instant)) {
        "TODAY" -> "Сегодня"
        "YESTERDAY" -> "Вчера"
        else -> "На прошлой неделе"
    }

    private fun drawSeparator(canvas: Canvas, recyclerView: RecyclerView, view: View, label: String) {
        val density = view.context.resources.displayMetrics.density

        val rectHeight = 48f * density
        val paddingVertical = 4f * density

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 14f * density
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        val fm = textPaint.fontMetrics
        val textHeight = fm.descent - fm.ascent

        val left = recyclerView.paddingLeft.toFloat()
        val right = (recyclerView.width - recyclerView.paddingRight).toFloat()
        val top = view.top.toFloat() - paddingVertical - rectHeight
        val bottom = view.top.toFloat() - paddingVertical

        val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0E0E0")
            style = Paint.Style.FILL
        }
        canvas.drawRect(left, top, right, bottom, rectPaint)

        val xText = (left + right) / 2f
        val centerY = (top + bottom) / 2f
        val yText = centerY + (textHeight / 2f) - fm.descent

        canvas.drawText(label, xText, yText, textPaint)
    }

    private fun parseInstant(published: String): Instant = Instant.parse(published)
}