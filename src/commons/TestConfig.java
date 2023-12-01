package src.commons;

import java.util.ArrayList;
import java.util.List;

public class TestConfig {
    public static final ArrayList<B> LIST_B;
    
    static {
        LIST_B = new ArrayList<>();
        LIST_B.add(new B());
        LIST_B.add(new B());
    }
}

class B {
    public static final ArrayList<C> LIST_C;

    static {
        LIST_C = new ArrayList<>();
        LIST_C.add(new C());
        LIST_C.add(new C());
    }
}

class C {
    // Clase C
}