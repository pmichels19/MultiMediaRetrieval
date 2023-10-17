package Rendering;

public enum ShadingMode {
    FLAT {
        @Override
        public String getDirectory() {
            return "flat";
        }
    },
    GOURAUD{
        @Override
        public String getDirectory() {
            return "gouraud";
        }
    };

    public abstract String getDirectory();
}
