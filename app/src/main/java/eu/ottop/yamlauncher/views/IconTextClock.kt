package eu.ottop.yamlauncher.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.ReplacementSpan
import android.util.AttributeSet
import android.widget.TextClock
import androidx.core.content.ContextCompat
import eu.ottop.yamlauncher.R
import kotlin.math.roundToInt

/**
 * A TextClock that draws [TRAIN_ICON] placeholders as a small monochrome train,
 * tinted and sized to match the text. Unicode's only train characters are colour
 * emoji, which clash with the plain text date line.
 *
 * The placeholder stays in the text, so character offsets (used for tap
 * detection) are unchanged.
 */
class IconTextClock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextClock(context, attrs, defStyleAttr) {

    /** Colour for the weather symbol; null draws it in the text colour. The train icon stays in the text colour. */
    var iconColor: Int? = null
        set(value) {
            if (field == value) return
            field = value
            text = text?.toString()
        }

    override fun setText(text: CharSequence?, type: BufferType?) {
        val accent = iconColor
        val hasWeather = accent != null && text?.any { it in WEATHER_ICONS } == true
        if (text == null || (!text.contains(TRAIN_ICON) && !hasWeather)) {
            super.setText(text, type)
            return
        }
        val spannable = SpannableString(text)
        var index = text.indexOf(TRAIN_ICON)
        while (index >= 0) {
            spannable.setSpan(IconSpan(), index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            index = text.indexOf(TRAIN_ICON, index + 1)
        }
        if (accent != null) {
            text.forEachIndexed { i, c ->
                if (c in WEATHER_ICONS) {
                    // Keep the text-style selector (U+FE0E) in the same span, or the
                    // glyph falls back to its colour emoji.
                    val end = if (text.getOrNull(i + 1) == '\uFE0E') i + 2 else i + 1
                    spannable.setSpan(ForegroundColorSpan(accent), i, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        super.setText(spannable, BufferType.SPANNABLE)
    }

    /** Draws the train glyph in the paint's colour, about cap height tall, sitting on the baseline. */
    private inner class IconSpan : ReplacementSpan() {
        private val drawable: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.ic_date_train)?.mutate()

        private fun size(paint: Paint): Int = (paint.textSize * 0.85f).roundToInt()

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            fm?.let { paint.getFontMetricsInt(it) }
            return size(paint)
        }

        override fun draw(
            canvas: Canvas, text: CharSequence?, start: Int, end: Int,
            x: Float, top: Int, y: Int, bottom: Int, paint: Paint
        ) {
            val d = drawable ?: return
            val s = size(paint)
            // Line the icon's bottom up with the baseline, nudged down slightly so
            // it sits optically level with digits.
            val bottomY = y + (paint.textSize * 0.05f).roundToInt()
            d.setBounds(0, 0, s, s)
            d.setTint(paint.color)
            d.alpha = paint.alpha
            canvas.save()
            canvas.translate(x, (bottomY - s).toFloat())
            d.draw(canvas)
            canvas.restore()
        }
    }

    companion object {
        /** The weather glyphs WeatherSystem uses. */
        private const val WEATHER_ICONS = "☀☁☂❄⛈"

        /** Private-use character marking where the train icon goes. */
        const val TRAIN_ICON = ''
    }
}
