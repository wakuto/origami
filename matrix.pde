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

equation generate_equation(vector a, vector b) {
    float x = b.x - a.x;
    float y = b.y - a.y;
    float tilt = y/x;
    float intercept = a.y - a.x * y/x;
    equation equ = new equation(tilt, intercept);
    return equ;
}

void line(vector a, vector b) {
    line(a.x, a.y, b.x, b.y);
}

void graph(equation e) {
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