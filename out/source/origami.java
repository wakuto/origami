import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class origami extends PApplet {

ArrayList<vector> vertex = new ArrayList<vector>();
ArrayList<edge> edges = new ArrayList<edge>();
ArrayList<edge> inner_edges = new ArrayList<edge>();
int click_count = 0;
boolean can_vertex_add = true;

float d = 10;  // 辺からの距離

public void setup() {
    
    background(0xffFFFFFF);
}

public void draw() {
}

public void keyReleased() {
    switch(key) {
        // clear
        case 'c': {
            background(0xffFFFFFF);
            click_count = 0;
            can_vertex_add = true;
            vertex = new ArrayList<vector>();
            edges = new ArrayList<edge>();
            break;
        }
        // determine vertex
        case '\n': {
            if(!can_vertex_add) break;
            vector a = vertex.get(0);
            vector b = vertex.get(vertex.size() - 1);
            line(a.x, a.y, b.x, b.y);
            can_vertex_add = false;
            break;
        }
        // run process
        case 'r': {
            if(can_vertex_add) break;

            // edgesの情報を初期化
            edges = initialize_edges(vertex);

            // 外側から内向き単位ベクトルのd倍内側にinner_edgesを描画
            inner_edges = change_distance_from_edge(edges, d);
            
            // それぞれの直線に対して垂線方向にd移動させた直線群を生成
            break;
        }
        
        case 's': {
            d += 1;
            inner_edges = change_distance_from_edge(edges, d);
            break;
        }
        
        case 'p': {
            background(0xffFFFFFF);
            print_polygon(edges);
            print_polygon(inner_edges);
        }
    }
}

public void mouseReleased() {
    if(can_vertex_add) {
        vertex.add(new vector(mouseX, mouseY));
        circle(mouseX, mouseY, 3);
        if(++click_count >= 2) {
            vector a = vertex.get(vertex.size() - 2);
            vector b = vertex.get(vertex.size() - 1);
            line(a.x, a.y, b.x, b.y);
        }
    }
}

public ArrayList<edge> change_distance_from_edge(ArrayList<edge> polygonal_sides, float distance) {
    ArrayList<edge> inneredges = new ArrayList<edge>();
    // 内向き単位ベクトルのd倍の位置にグラフを描画
    for(edge e: polygonal_sides) {
        vector in = e.in.clone();
        vector center = e.center.clone();
        in.sub(center);
        in.mul(distance);
        in.add(center);
        float b = in.y - e.equ.a * in.x;
        inneredges.add(new edge(new equation(e.equ.a, b)));
    }
    
    // 内側の線の交点を求める
    edge eg;
    for(int i = 0; i < inneredges.size()-1; i++) {
        eg = inneredges.get(i);
        eg.b = eg.equ.intersection(inneredges.get(i+1).equ);
    }
    eg = inneredges.get(inneredges.size()-1);
    eg.b = eg.equ.intersection(inneredges.get(0).equ);
    for(int i = inneredges.size()-1; i > 0; i--) {
        eg = inneredges.get(i);
        eg.a = inneredges.get(i-1).b;
    }
    eg = inneredges.get(inneredges.size()-1);
    inneredges.get(0).a = eg.b;
    
    for(edge e: inneredges) {
        e.init();
        circle(e.a.x, e.a.y, 5);
        line(e.a, e.b);
    }
    return inneredges;
}

public void straight_skeleton() {
    /*
    辺を内側に寄せていく方法
    →辺の上を通る方程式を求める
    その方程式の垂線方向に
    */
}

public ArrayList<edge> initialize_edges(ArrayList<vector> vertexes) {
    ArrayList<edge> polygonal_sides = new ArrayList<edge>();

    // すべての辺に対する直線を生成
    for(int i = 0; i < vertexes.size()-1; i++) {
        polygonal_sides.add(new edge(vertexes.get(i), vertexes.get(i+1)));
    }
    polygonal_sides.add(new edge(vertexes.get(vertexes.size()-1), vertexes.get(0)));
    
    for(int i = 0; i < polygonal_sides.size(); i++) {
        // 中点より右/左の交点の数
        int r = 0, l = 0;

        // i の垂直二等分線との交点を求める
        edge eg = polygonal_sides.get(i);
        for(int j = 0; j < polygonal_sides.size(); j++) {
            if(j==i) continue;
            edge e = polygonal_sides.get(j);
            vector v = new vector(0,0);
            try {
                v = eg.perpendicular.intersection(e.equ);
                graph(eg.perpendicular);
                // vが辺の上にあるなら追加
                if(e.a.x <  e.b.x && e.a.x <= v.x && v.x <= e.b.x ||
                   e.a.x >= e.b.x && e.b.x <= v.x && v.x <= e.a.x) {
                    // 交点のカウント
                    if(eg.center.x < v.x)
                        r++;
                    else
                        l++;

                    circle(v.x, v.y, 5);
                    eg.intersec.add(v);
                } 
            } catch(Exception exception) {
                println(exception);
            }
        }

        float x = eg.center.x;
        if(r % 2 == 1) x+=1;
        else if(l % 2 == 1) x-=1;
        else println("内側の判定でエラーが発生しました");
        float y = eg.perpendicular.a * x + eg.perpendicular.b;
        vector vec = new vector(x, y);
        vec.sub(eg.center);
        vec.normalize();
        vec.add(eg.center);
        eg.in = vec;
    }
    
    return polygonal_sides;
}

public void print_polygon(ArrayList<edge> eg) {
    for(edge e: eg) {
        line(e.a, e.b);
    }
}
class vector {
    public float x, y;
    public vector(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public vector normalize() {
        float size = x * x + y * y;
        size = sqrt(size);
        this.mul(1/size);
        return this;
    }
    
    public vector add(vector v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }
    
    public vector sub(vector v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }
    
    public vector mul(float c) {
        this.x *= c;
        this.y *= c;
        return this;
    }
    
    public vector clone() {
        vector clone = new vector(x, y);
        return clone;
    }
}

// 2D matrix
class matrix {
    float[][] mat = new float[2][2];
    public matrix(float a, float b, float c, float d) {
        mat[0][0] = a;
        mat[0][1] = b;
        mat[1][0] = c;
        mat[1][1] = d;
    }
    public matrix(vector a, vector b) {
        mat[0][0] = a.x;
        mat[0][1] = b.x;
        mat[1][0] = a.y;
        mat[1][1] = b.y;
    }
    
    public float determinant() {
        return mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0];
    }
    public void set(int x, int y, float num) {
        mat[y][x] = num;
    }
    public float[][] get() {
        return mat;
    }
    public float get(int x, int y) {
        return mat[y][x];
    }
}

class equation {
    // y = ax + b
    public float a, b;
    public equation(float a, float b) {
        this.a = a;
        this.b = b;
    }
    // 交点
    public vector intersection(equation eq) {
        matrix x = new matrix(new vector(a, eq.a), new vector(-1, -1));
        float determinant = x.determinant();

        x.set(0, 0, -b);
        x.set(0, 1, -eq.b);
        matrix y = new matrix(new vector(a, eq.a), new vector(-b, -eq.b));
        // a, b = Δ
        vector vec = new vector(x.determinant() / determinant, y.determinant() / determinant);
        return vec;
    }
}

class edge {
    public vector a, b;             // 始点・終点
    public vector center;           // 中点
    public vector in;               // 中点->内側を示す単位ベクトル
    public equation equ;            // a, bを通る直線の式
    public equation perpendicular;  // equの垂直二等分線の式
    public ArrayList<vector> intersec = new ArrayList<vector>();
    private boolean equation_initialize = false;// 初期化時にedge(equation)を使ったらtrue
    public edge(vector a, vector b) {
        this.a = a;
        this.b = b;
        equ = generate_equation(a, b);
        float p = (a.x+b.x)/2;
        float q = (a.y+b.y)/2;
        center = new vector(p, q);
        perpendicular = new equation(-1/equ.a, q - (-1/equ.a)*p);
    }
    
    public edge(equation e) {
        this.equ = e;
        equation_initialize = true;
    }
    
    // execute after setting the start point and the end point.
    public void init() {
        if(!equation_initialize || a == null || b == null) return;

        float p = (a.x+b.x)/2;
        float q = (a.y+b.y)/2;
        center = new vector(p, q);
        perpendicular = new equation(-1/equ.a, q - (-1/equ.a)*p);
    }
}

public equation generate_equation(vector a, vector b) {
    float x = b.x - a.x;
    float y = b.y - a.y;
    float tilt = y/x;
    float intercept = a.y - a.x * y/x;
    equation equ = new equation(tilt, intercept);
    return equ;
}

public void line(vector a, vector b) {
    line(a.x, a.y, b.x, b.y);
}

public void graph(equation e) {
    vector left;
    vector right;
    if(e.b >= 0) {
        left = new vector(0, e.b);
    } else {
        left = new vector(-e.b/e.a, 0);
    }
    if(e.a*width+e.b <= height) {
        right = new vector(width, e.a*width+e.b);
    } else {
        right = new vector((height-e.b)/e.a, height);
    }
    line(left, right);
}
  public void settings() {  size(480, 480); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "origami" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
