package Model.Cache;

public class CacheBlock {
    private final long tag;
    private byte[] data;
    private boolean valid;
    private boolean dirty;

    public CacheBlock(long tag, int blockSize) {
        this.tag = tag;
        this.data = new byte[blockSize];
        this.valid = true;
        this.dirty = false;
    }

    public long getTag() {
        return tag;
    }

    public byte[] getData() {
        return data;
    }
    public void setData(byte[] data) {
        this.data = data;
    }
    public boolean isValid() {
        return valid;
    }
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    public boolean isDirty() {
        return this.dirty;
    }
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
