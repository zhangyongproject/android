/*
 * Copyright (c) 2010, Sony Ericsson Mobile Communication AB. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *    * Neither the name of the Sony Ericsson Mobile Communication AB nor the names
 *      of its contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.blue.uyou.gamelauncher.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;

/**
 * Class containing static utility methods for bitmap decoding and scaling
 * 
 * @author Andreas Agvard (andreas.agvard@sonyericsson.com)
 */
public class ScalingUtilities {

	public static final int nStart = 0;

	/**
	 * Utility function for decoding an image resource. The decoded bitmap will
	 * be optimized for further scaling to the requested destination dimensions
	 * and scaling logic.
	 * 
	 * @param res
	 *            The resources object containing the image data
	 * @param resId
	 *            The resource id of the image data
	 * @param dstWidth
	 *            Width of destination area
	 * @param dstHeight
	 *            Height of destination area
	 * @param scalingLogic
	 *            Logic to use to avoid image stretching
	 * @return Decoded bitmap
	 */
	public static Bitmap decodeResource(Resources res, int resId, int dstWidth,
			int dstHeight, ScalingLogic scalingLogic) {
		Options options = new Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		options.inJustDecodeBounds = false;
		options.inSampleSize = calculateSampleSize(options.outWidth,
				options.outHeight, dstWidth, dstHeight, scalingLogic);
		Bitmap unscaledBitmap = BitmapFactory.decodeResource(res, resId,
				options);

		return unscaledBitmap;
	}

	/**
	 * Utility function for creating a scaled version of an existing bitmap
	 * 
	 * @param unscaledBitmap
	 *            Bitmap to scale
	 * @param dstWidth
	 *            Wanted width of destination bitmap
	 * @param dstHeight
	 *            Wanted height of destination bitmap
	 * @param scalingLogic
	 *            Logic to use to avoid image stretching
	 * @return New scaled bitmap object
	 */
	public static Bitmap createScaledBitmap(Bitmap unscaledBitmap,
			int dstWidth, int dstHeight, ScalingLogic scalingLogic,
			boolean isBottomColor) {
		if (unscaledBitmap != null && unscaledBitmap.isRecycled()) {
			return null;
		}
		Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(),
				unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
		Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(),
				unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
		Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(),
				dstRect.height(), Config.ARGB_8888);
		Canvas canvas = new Canvas(scaledBitmap);
		if (isBottomColor)
			canvas.drawColor(0xFF222222);
		canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(
				Paint.FILTER_BITMAP_FLAG));
		return scaledBitmap;
	}

	/**
	 * ScalingLogic defines how scaling should be carried out if source and
	 * destination image has different aspect ratio.
	 * 
	 * CROP: Scales the image the minimum amount while making sure that at least
	 * one of the two dimensions fit inside the requested destination area.
	 * Parts of the source image will be cropped to realize this.
	 * 
	 * FIT: Scales the image the minimum amount while making sure both
	 * dimensions fit inside the requested destination area. The resulting
	 * destination dimensions might be adjusted to a smaller size than
	 * requested.
	 */
	public static enum ScalingLogic {
		CROP, FIT
	}

	/**
	 * Calculate optimal down-sampling factor given the dimensions of a source
	 * image, the dimensions of a destination area and a scaling logic.
	 * 
	 * @param srcWidth
	 *            Width of source image
	 * @param srcHeight
	 *            Height of source image
	 * @param dstWidth
	 *            Width of destination area
	 * @param dstHeight
	 *            Height of destination area
	 * @param scalingLogic
	 *            Logic to use to avoid image stretching
	 * @return Optimal down scaling sample size for decoding
	 */
	public static int calculateSampleSize(int srcWidth, int srcHeight,
			int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
		if (scalingLogic == ScalingLogic.FIT) {
			final float srcAspect = (float) srcWidth / (float) srcHeight;
			final float dstAspect = (float) dstWidth / (float) dstHeight;

			if (srcAspect > dstAspect) {
				return srcWidth / dstWidth;
			} else {
				return srcHeight / dstHeight;
			}
		} else {
			final float srcAspect = (float) srcWidth / (float) srcHeight;
			final float dstAspect = (float) dstWidth / (float) dstHeight;

			if (srcAspect > dstAspect) {
				return srcHeight / dstHeight;
			} else {
				return srcWidth / dstWidth;
			}
		}
	}

	/**
	 * Calculates source rectangle for scaling bitmap
	 * 
	 * @param srcWidth
	 *            Width of source image
	 * @param srcHeight
	 *            Height of source image
	 * @param dstWidth
	 *            Width of destination area
	 * @param dstHeight
	 *            Height of destination area
	 * @param scalingLogic
	 *            Logic to use to avoid image stretching
	 * @return Optimal source rectangle
	 */
	public static Rect calculateSrcRect(int srcWidth, int srcHeight,
			int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
		if (scalingLogic == ScalingLogic.CROP) {
			final float srcAspect = (float) srcWidth / (float) srcHeight;
			final float dstAspect = (float) dstWidth / (float) dstHeight;

			if (srcAspect > dstAspect) {
				final int srcRectWidth = (int) (srcHeight * dstAspect);
				final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
				return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth,
						srcHeight);
			} else {
				final int srcRectHeight = (int) (srcWidth / dstAspect);
				final int scrRectTop = (int) (srcHeight - srcRectHeight) / 2;
				return new Rect(0, scrRectTop, srcWidth, scrRectTop
						+ srcRectHeight);
			}
		} else {
			return new Rect(0, 0, srcWidth, srcHeight);
		}
	}

	/**
	 * Calculates destination rectangle for scaling bitmap
	 * 
	 * @param srcWidth
	 *            Width of source image
	 * @param srcHeight
	 *            Height of source image
	 * @param dstWidth
	 *            Width of destination area
	 * @param dstHeight
	 *            Height of destination area
	 * @param scalingLogic
	 *            Logic to use to avoid image stretching
	 * @return Optimal destination rectangle
	 */
	public static Rect calculateDstRect(int srcWidth, int srcHeight,
			int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
		if (scalingLogic == ScalingLogic.FIT) {
			final float srcAspect = (float) srcWidth / (float) srcHeight;
			final float dstAspect = (float) dstWidth / (float) dstHeight;

			if (srcAspect > dstAspect) {
				return new Rect(0, 0, dstWidth, (int) (dstWidth / srcAspect));
			} else {
				return new Rect(0, 0, (int) (dstHeight * srcAspect), dstHeight);
			}
		} else {
			return new Rect(0, 0, dstWidth, dstHeight);
		}
	}

	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap,
			int desWidth, int desHeight) {
		Bitmap scaledBitmap = createScaledBitmap(bitmap, desWidth, desHeight,
				ScalingLogic.CROP, false);
		scaledBitmap = createReflectedImage(scaledBitmap);
		return scaledBitmap;
	}

	public static Bitmap createReflectedImage(Bitmap originalImage) {
		// The gap we want between the reflection and the original image
		final int reflectionGap = 0;

		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		// This will not scale but will flip on the Y axis
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		// Create a Bitmap with the flip matrix applied to it.
		// We only want the bottom half of the image
		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, nStart,
				nStart, width, height, matrix, false);

		// Create a new bitmap with same width but taller to fit reflection
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height), Config.ARGB_8888);

		// Create a new Canvas with the bitmap that's big enough for
		// the image plus gap plus reflection
		Canvas canvas = new Canvas(bitmapWithReflection);
		// Draw in the original image
		canvas.drawBitmap(originalImage, nStart, nStart, null);
		// Draw in the gap
		Paint defaultPaint = new Paint();
		canvas.drawRect(nStart, height, width, height + reflectionGap,
				defaultPaint);
		// Draw in the reflection
		canvas.drawBitmap(reflectionImage, nStart, height + reflectionGap, null);

		// Create a shader that is a linear gradient that covers the reflection
		Paint paint = new Paint();
		// LinearGradient shader = new LinearGradient(nStart,
		// originalImage.getHeight(), nStart,
		// bitmapWithReflection.getHeight() + reflectionGap, 0x00000000,
		// 0x000000, TileMode.CLAMP);
		LinearGradient shader = new LinearGradient(nStart,
				originalImage.getHeight(), nStart,
				bitmapWithReflection.getHeight() + reflectionGap, 0x82000000,
				0xff000000, TileMode.MIRROR);
		// //Set the paint to use this shader (linear gradient)
		paint.setShader(shader);
		// paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Set the Transfer mode to be porter duff and destination in
		// paint.setXfermode(new PorterDuffXfermode(Mode.AVOID));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(nStart, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		return bitmapWithReflection;
	}

}
