ArrayList<vector> vertex = new ArrayList<vector>();
ArrayList<edge> edges = new ArrayList<edge>();
ArrayList<edge> inner_edges = new ArrayList<edge>();
int click_count = 0;
boolean can_vertex_add = true;

float d = 10;  // 辺からの距離

void setup() {
    size(480, 480);
    background(#FFFFFF);
}

void draw() {
}

void keyReleased() {
    switch(key) {
        // clear
        case 'c': {
            background(#FFFFFF);
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
            background(#FFFFFF);
            print_polygon(edges);
            print_polygon(inner_edges);
        }
    }
}

void mouseReleased() {
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

ArrayList<edge> change_distance_from_edge(ArrayList<edge> polygonal_sides, float distance) {
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

void straight_skeleton() {
    /*
    辺を内側に寄せていく方法
    →辺の上を通る方程式を求める
    その方程式の垂線方向に
    */
}

ArrayList<edge> initialize_edges(ArrayList<vector> vertexes) {
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

void print_polygon(ArrayList<edge> eg) {
    for(edge e: eg) {
        line(e.a, e.b);
    }
}