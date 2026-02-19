package model.cache;

public class CacheBlock {
    private long tag;
    private byte[] data;
    private boolean valid;
    private boolean dirty;

    public CacheBlock(long tag, int blockSize) {
        this.tag = tag;
        this.data = new byte[blockSize];
        this.valid = false;
        this.dirty = false;
    }
    public long getTag() {
        return tag;
    }
    public void setTag(long tag) {
        this.tag = tag;
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
