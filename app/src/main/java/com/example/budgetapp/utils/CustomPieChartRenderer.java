package com.example.budgetapp.utils;


import android.graphics.Canvas;
import android.graphics.Paint;
import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import java.util.Collection;
import java.util.List;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

@Metadata(
        mv = {1, 1, 16},
        bv = {1, 0, 3},
        k = 1,
        d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005¢\u0006\u0002\u0010\u0006J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0016R\u0011\u0010\u0004\u001a\u00020\u0005¢\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b¨\u0006\r"},
        d2 = {"Lcom/example/budgetapp/home/CustomPieChartRenderer;", "Lcom/github/mikephil/charting/renderer/PieChartRenderer;", "pieChart", "Lcom/github/mikephil/charting/charts/PieChart;", "circleRadius", "", "(Lcom/github/mikephil/charting/charts/PieChart;F)V", "getCircleRadius", "()F", "drawValues", "", "c", "Landroid/graphics/Canvas;", "app_debug"}
)
public final class CustomPieChartRenderer extends PieChartRenderer {
    private final float circleRadius;

    public void drawValues(@NotNull Canvas c) {
        Intrinsics.checkParameterIsNotNull(c, "c");
        super.drawValues(c);
        PieChart var10000 = this.mChart;
        Intrinsics.checkExpressionValueIsNotNull(var10000, "mChart");
        MPPointF center = var10000.getCenterCircleBox();
        var10000 = this.mChart;
        Intrinsics.checkExpressionValueIsNotNull(var10000, "mChart");
        float radius = var10000.getRadius();
        var10000 = this.mChart;
        Intrinsics.checkExpressionValueIsNotNull(var10000, "mChart");
        float rotationAngle = var10000.getRotationAngle();
        var10000 = this.mChart;
        Intrinsics.checkExpressionValueIsNotNull(var10000, "mChart");
        float[] drawAngles = var10000.getDrawAngles();
        var10000 = this.mChart;
        Intrinsics.checkExpressionValueIsNotNull(var10000, "mChart");
        float[] absoluteAngles = var10000.getAbsoluteAngles();
        ChartAnimator var31 = this.mAnimator;
        Intrinsics.checkExpressionValueIsNotNull(var31, "mAnimator");
        float phaseX = var31.getPhaseX();
        var31 = this.mAnimator;
        Intrinsics.checkExpressionValueIsNotNull(var31, "mAnimator");
        float phaseY = var31.getPhaseY();
        PieChart var10002 = this.mChart;
        Intrinsics.checkExpressionValueIsNotNull(var10002, "mChart");
        float roundedRadius = (radius - radius * var10002.getHoleRadius() / 100.0F) / 2.0F;
        var10000 = this.mChart;
        Intrinsics.checkExpressionValueIsNotNull(var10000, "mChart");
        float holeRadiusPercent = var10000.getHoleRadius() / 100.0F;
        float labelRadiusOffset = radius / 10.0F * 3.6F;
        var10000 = this.mChart;
        Intrinsics.checkExpressionValueIsNotNull(var10000, "mChart");
        if (var10000.isDrawHoleEnabled()) {
            labelRadiusOffset = (radius - radius * holeRadiusPercent) / 2.0F;
            var10000 = this.mChart;
            Intrinsics.checkExpressionValueIsNotNull(var10000, "mChart");
            if (!var10000.isDrawSlicesUnderHoleEnabled()) {
                var10000 = this.mChart;
                Intrinsics.checkExpressionValueIsNotNull(var10000, "mChart");
                if (var10000.isDrawRoundedSlicesEnabled()) {
                    rotationAngle += roundedRadius * (float)360 / (float)(6.283185307179586D * (double)radius);
                }
            }
        }

        float labelRadius = radius - labelRadiusOffset;
        var10000 = this.mChart;
        Intrinsics.checkExpressionValueIsNotNull(var10000, "mChart");
        PieData var32 = (PieData)var10000.getData();
        Intrinsics.checkExpressionValueIsNotNull(var32, "mChart.data");
        List dataSets = var32.getDataSets();
        float angle = 0.0F;
        int xIndex = 0;
        c.save();
        int i = 0;
        Intrinsics.checkExpressionValueIsNotNull(dataSets, "dataSets");

        for(int var17 = ((Collection)dataSets).size(); i < var17; ++i) {
            IPieDataSet dataSet = (IPieDataSet)dataSets.get(i);
            float sliceSpace = this.getSliceSpace(dataSet);
            int j = 0;
            Intrinsics.checkExpressionValueIsNotNull(dataSet, "dataSet");

            for(int var21 = dataSet.getEntryCount(); j < var21; ++j) {
                angle = xIndex == 0 ? 0.0F : absoluteAngles[xIndex - 1] * phaseX;
                float sliceAngle = drawAngles[xIndex];
                float sliceSpaceMiddleAngle = sliceSpace / (0.017453292F * labelRadius);
                angle += (sliceAngle - sliceSpaceMiddleAngle / 2.0F) / 2.0F;
                if (dataSet.getValueLineColor() != 1122867) {
                    float transformedAngle = rotationAngle + angle * phaseY;
                    float sliceXBase = (float)Math.cos((double)transformedAngle * (double)0.017453292F);
                    float sliceYBase = (float)Math.sin((double)transformedAngle * (double)0.017453292F);
                    float valueLinePart1OffsetPercentage = dataSet.getValueLinePart1OffsetPercentage() / 100.0F;
                    var10000 = this.mChart;
                    Intrinsics.checkExpressionValueIsNotNull(var10000, "mChart");
                    float line1Radius = var10000.isDrawHoleEnabled() ? (radius - radius * holeRadiusPercent) * valueLinePart1OffsetPercentage + radius * holeRadiusPercent : radius * valueLinePart1OffsetPercentage;
                    float px = line1Radius * sliceXBase + center.getX();
                    float py = line1Radius * sliceYBase + center.getY();
                    if (dataSet.isUsingSliceColorAsValueLineColor()) {
                        Paint var33 = this.mRenderPaint;
                        Intrinsics.checkExpressionValueIsNotNull(var33, "mRenderPaint");
                        var33.setColor(dataSet.getColor(j));
                    }

                    c.drawCircle(px, py, this.circleRadius, this.mRenderPaint);
                }

                ++xIndex;
            }
        }

        MPPointF.recycleInstance(center);
        c.restore();
    }

    public final float getCircleRadius() {
        return this.circleRadius;
    }

    public CustomPieChartRenderer(@NotNull PieChart pieChart, float circleRadius) {
        super(pieChart, pieChart.getAnimator(), pieChart.getViewPortHandler());
        Intrinsics.checkParameterIsNotNull(pieChart, "pieChart");
        this.circleRadius = circleRadius;
    }
}

