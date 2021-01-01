package edu.real.android.plan;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.EditText;
import edu.real.cross.StringTools;

/**
 *  EditText's subclass with specially overridden `onSelectionChanged`.
 *  */
/* TODO: extract generic edu.real.android.REditText */
public class RPlanEditText extends EditText {

	/* Use Context instead of TaskEditActivity for compatibility with
	 * visual designer. */
	public RPlanEditText(Context context)
	{
		super(context);
	}

	public RPlanEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public RPlanEditText(Context context, AttributeSet attrs,
			int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	public RPlanEditText(Context context, AttributeSet attrs,
			int defStyleAttr,
			int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onSelectionChanged(int selStart, int selEnd)
	{
		super.onSelectionChanged(selStart, selEnd);

		if (isInEditMode()) {
			return;
		}

		/*
		Log.v(this.getClass().getName(),
			String.format("Sel: %d %d", selStart, selEnd));
		*/

		SpannableStringBuilder text = (SpannableStringBuilder) getText();

		if (selStart == selEnd) {
			/* Try to format word at cursor. */
			int word[] = new int[2];
			if (StringTools.wordAt(text.toString(), selStart, word)) {
				selStart = word[0];
				selEnd = word[1];
			} else {
				return;
			}
		}

		int next_i;
		StyleSpan spans[];

		int total = 0, bold = 0, italic = 0;

		for (int i = selStart; i < selEnd; i = next_i) {
			next_i = text.nextSpanTransition(i, selEnd, StyleSpan.class);
			spans = text.getSpans(i, next_i, StyleSpan.class);

			total++;

			for (StyleSpan span : spans) {
				if ((span.getStyle() & Typeface.BOLD) != 0) {
					bold++;
					break;
				}
			}

			for (StyleSpan span : spans) {
				if ((span.getStyle() & Typeface.ITALIC) != 0) {
					italic++;
					break;
				}
			}
		}

		TaskEditActivity tea = (TaskEditActivity) getContext();

		tea.tb_bold.setOnCheckedChangeListener(null);
		tea.tb_italic.setOnCheckedChangeListener(null);

		if (total > 0) {
			tea.tb_bold.setChecked(bold == total);
			tea.tb_italic.setChecked(italic == total);
		} else {
			tea.tb_bold.setChecked(false);
			tea.tb_italic.setChecked(false);
		}

		tea.tb_bold.setOnCheckedChangeListener(tea);
		tea.tb_italic.setOnCheckedChangeListener(tea);
	}

}
