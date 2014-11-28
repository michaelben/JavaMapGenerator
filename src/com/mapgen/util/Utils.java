package com.mapgen.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import math.geom2d.Point2D;

public class Utils {
    
    public static ArrayList<Point2D> generateRandomPoints(int size, Range range) {
        Random random = new Random();
        HashSet<Point2D> points = new HashSet<Point2D>();
        double x = range.getMinX();
        double y = range.getMinY();
        double width = range.getWidth();
        double height = range.getHeight();
        while (points.size() < size)
            points.add(new Point2D(Math.floor(random.nextFloat()*width + x),
                                 Math.floor(random.nextFloat()*height + y)));

        return new ArrayList<Point2D>(points);
    }
    
    public static ArrayList<Point> testPoints5() {
        ArrayList<Point> points = new ArrayList<Point>(5);
        points.add(new Point(-6,4));
        points.add(new Point(3,1));
        points.add(new Point(2,-3));
        points.add(new Point(-2,2));
        points.add(new Point(5,5));
        return points;
    }
    
    public static ArrayList<Point> testPoints7() {
        ArrayList<Point> points = new ArrayList<Point>(7);
        points.add(new Point(2,2));
        points.add(new Point(-2,5));
        points.add(new Point(6,-3));
        points.add(new Point(-1,-7));
        points.add(new Point(-9,-1));
        points.add(new Point(5,3));
        points.add(new Point(-3,-1));
        return points;
    }
    
    public static ArrayList<Point> testPoints10() {
        ArrayList<Point> points = new ArrayList<Point>(10);
        points.add(new Point(-1,2));
        points.add(new Point(7,7));
        points.add(new Point(-6,4));
        points.add(new Point(1,-3));
        points.add(new Point(3,-5));
        points.add(new Point(3,4));
        points.add(new Point(-8,5));
        points.add(new Point(-6,-4));
        points.add(new Point(-2,6));
        points.add(new Point(4,-1));
        return points;
    }
    
    public static ArrayList<Point> testPoints11() {
        ArrayList<Point> points = new ArrayList<Point>(10);
        points.add(new Point(23,264));
        points.add(new Point(139,116));
        points.add(new Point(857,323));
        points.add(new Point(949,746));
        points.add(new Point(1087,734));
        points.add(new Point(1022,166));
        points.add(new Point(352,412));
        points.add(new Point(275,720));
        points.add(new Point(1196,160));
        points.add(new Point(1216,550));
        points.add(new Point(1028,569));
        return points;
    }
    
    public static ArrayList<Point> testPoints11a() {
        ArrayList<Point> points = new ArrayList<Point>(10);
        points.add(new Point(1058,143));
        points.add(new Point(228,565));
        points.add(new Point(450,343));
        points.add(new Point(1128,789));
        points.add(new Point(119,620));
        points.add(new Point(581,38));
        points.add(new Point(919,468));
        points.add(new Point(780,371));
        points.add(new Point(36,307));
        points.add(new Point(1052,174));
        points.add(new Point(985,251));
        return points;
    }
    
    public static void printPointsInRange(float x1, float y1, float x2, float y2, List<Point> points){
        Range r = new Range(new Point(x1,y1),new Point(x2,y2));
        
        for (Point point : points) {
            if(r.isInRange(point)){
                System.out.println("points.add(new Point(" + point.x + "f," + point.y + "f));");
            }
        }
    }
    
    public static void printPoints(List<Point> points){
        for (int i = 0; i < points.size(); i++) {  
            Point point = points.get(i);
            System.out.println("points.add(new Point(" + point.x + "f," + point.y + "f));");
        }
    }
    
    public static void sort(double dist[], int indexlist[]){
        quicksort(dist, indexlist, 0, dist.length-1);  
    }
    
    public static void quicksort(double matrix[], int indexList[], int a, int b) {
        double buf;
        int buf2;
        int from = a;
        int to = b;
        double pivot = matrix[(from + to) / 2];
        do {
            while (pivot>matrix[from]) {
                from++;
            }
            while (pivot<matrix[to]) {
                to--;
            }
            if (from <= to) {
                buf = matrix[from];
                buf2 = indexList[from];
                matrix[from] = matrix[to];
                indexList[from] = indexList[to];
                matrix[to] = buf;
                indexList[to] = buf2;
                from++;
                to--;
            }
        } while (from <= to);
        if (a < to) {
            quicksort(matrix,indexList, a, to);
        }
        if (from < b) {
            quicksort(matrix,indexList, from, b);
        }
    }
}
