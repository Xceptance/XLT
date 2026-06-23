/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.report;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Computes the Structural Similarity Index Measure (SSIM) between two images.
 *
 * <p>SSIM is a perceptual similarity metric that reflects the human visual system more accurately than
 * pixel-by-pixel difference metrics. A score of <strong>1.0</strong> means the images are
 * pixel-perfect identical; a score of <strong>0.0 or below</strong> means no structural similarity at all.
 * In practice, scores above 0.95 indicate visually near-identical images.
 *
 * <h2>Algorithm</h2>
 * <p>The implementation uses the standard Wang et al. (2004) definition computed over luminance:
 * <pre>
 *   SSIM(x,y) = (2μxμy + C1)(2σxy + C2)
 *             / ((μx² + μy² + C1)(σx² + σy² + C2))
 * </pre>
 * where:
 * <ul>
 *   <li>μx, μy — mean luminance of patches x and y</li>
 *   <li>σx, σy — standard deviation of patches</li>
 *   <li>σxy — covariance of patches</li>
 *   <li>C1 = (0.01·L)², C2 = (0.03·L)², L = 255 (dynamic range)</li>
 * </ul>
 * Patches are non-overlapping 8×8 windows. The overall SSIM is the mean of all patch scores.
 *
 * <h2>Image loading</h2>
 * <p>Uses {@link ImageIO#read(File)}, so any format registered in the JVM is supported.
 * Because the project includes {@code webp-imageio} on the classpath, {@code .webp} files
 * are decoded transparently alongside PNG and JPEG.
 *
 * <h2>Size mismatch</h2>
 * <p>If the two images differ in size the smaller one is scaled up to match the larger using
 * bicubic interpolation before the comparison.
 */
public final class ChartSsim
{
    /** SSIM window size (pixels per side). */
    private static final int WINDOW = 8;

    /** Dynamic range of 8-bit images (L = 2^bits - 1). */
    private static final double L = 255.0;

    // Stabilisation constants (Wang et al. 2004 defaults)
    private static final double C1 = (0.01 * L) * (0.01 * L); // (K1·L)²
    private static final double C2 = (0.03 * L) * (0.03 * L); // (K2·L)²

    /** Utility class — not instantiable. */
    private ChartSsim()
    {
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Computes the SSIM score between two image files.
     *
     * @param a
     *            first image file (any {@link ImageIO}-readable format including WebP)
     * @param b
     *            second image file
     * @return SSIM in [0.0, 1.0] — 1.0 = identical, &lt;0.95 = noticeable difference
     * @throws IOException
     *             if either file cannot be read or decoded
     */
    public static double compute(final File a, final File b) throws IOException
    {
        BufferedImage imgA = readImage(a);
        BufferedImage imgB = readImage(b);

        // If sizes differ, scale the smaller to match the larger
        if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight())
        {
            final int w = Math.max(imgA.getWidth(), imgB.getWidth());
            final int h = Math.max(imgA.getHeight(), imgB.getHeight());
            if (imgA.getWidth() < w || imgA.getHeight() < h)
            {
                imgA = scale(imgA, w, h);
            }
            else
            {
                imgB = scale(imgB, w, h);
            }
        }

        final int width = imgA.getWidth();
        final int height = imgA.getHeight();

        final double[] lumA = toLuminance(imgA, width, height);
        final double[] lumB = toLuminance(imgB, width, height);

        return computeSsim(lumA, lumB, width, height);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Reads an image file using {@link ImageIO}. Initialises any registered ImageIO plugins
     * (including webp-imageio) so that WebP decoding works without manual plugin registration.
     */
    private static BufferedImage readImage(final File file) throws IOException
    {
        // Ensure all registered SPI providers (including webp-imageio) are active
        ImageIO.scanForPlugins();
        final BufferedImage img = ImageIO.read(file);
        if (img == null)
        {
            throw new IOException("ImageIO could not decode image: " + file.getAbsolutePath() +
                                  " — check that the format is supported (WebP requires webp-imageio on classpath)");
        }
        return img;
    }

    /**
     * Scales an image to the target dimensions using bicubic interpolation.
     */
    private static BufferedImage scale(final BufferedImage src, final int targetW, final int targetH)
    {
        final BufferedImage dst = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = dst.createGraphics();
        try
        {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(src, 0, 0, targetW, targetH, null);
        }
        finally
        {
            g.dispose();
        }
        return dst;
    }

    /**
     * Converts a {@link BufferedImage} to a flat array of linearised luminance values in [0, 255].
     * Luminance: 0.2126·R + 0.7152·G + 0.0722·B  (BT.709 coefficients).
     */
    private static double[] toLuminance(final BufferedImage img, final int width, final int height)
    {
        final double[] lum = new double[width * height];
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                final int rgb = img.getRGB(x, y);
                final double r = (rgb >> 16) & 0xFF;
                final double g = (rgb >> 8) & 0xFF;
                final double b = rgb & 0xFF;
                lum[y * width + x] = 0.2126 * r + 0.7152 * g + 0.0722 * b;
            }
        }
        return lum;
    }

    /**
     * Computes the mean SSIM across all non-overlapping WINDOW×WINDOW patches.
     */
    private static double computeSsim(final double[] a, final double[] b, final int width, final int height)
    {
        double ssimSum = 0.0;
        int patches = 0;

        for (int y = 0; y + WINDOW <= height; y += WINDOW)
        {
            for (int x = 0; x + WINDOW <= width; x += WINDOW)
            {
                ssimSum += patchSsim(a, b, x, y, width);
                patches++;
            }
        }

        return patches == 0 ? 1.0 : ssimSum / patches;
    }

    /**
     * Computes SSIM for a single WINDOW×WINDOW patch at (px, py).
     */
    private static double patchSsim(final double[] a, final double[] b,
                                    final int px, final int py, final int width)
    {
        double sumA = 0.0, sumB = 0.0;
        final int n = WINDOW * WINDOW;

        for (int dy = 0; dy < WINDOW; dy++)
        {
            for (int dx = 0; dx < WINDOW; dx++)
            {
                final int idx = (py + dy) * width + (px + dx);
                sumA += a[idx];
                sumB += b[idx];
            }
        }

        final double muA = sumA / n;
        final double muB = sumB / n;

        double varA = 0.0, varB = 0.0, covarAB = 0.0;

        for (int dy = 0; dy < WINDOW; dy++)
        {
            for (int dx = 0; dx < WINDOW; dx++)
            {
                final int idx = (py + dy) * width + (px + dx);
                final double dA = a[idx] - muA;
                final double dB = b[idx] - muB;
                varA += dA * dA;
                varB += dB * dB;
                covarAB += dA * dB;
            }
        }

        // Use population variance (divide by n, not n-1) — consistent with Wang et al.
        varA /= n;
        varB /= n;
        covarAB /= n;

        final double numerator = (2.0 * muA * muB + C1) * (2.0 * covarAB + C2);
        final double denominator = (muA * muA + muB * muB + C1) * (varA + varB + C2);

        return denominator == 0.0 ? 1.0 : numerator / denominator;
    }
}
