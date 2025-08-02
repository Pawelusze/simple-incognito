package pl.lambda.incognito.data;

public final class PlayerData {
    private final String originalNickname;
    private boolean nicknameHidden;
    private boolean skinHidden;
    private String fakeNickname;

    public PlayerData(String originalNickname) {
        this.originalNickname = originalNickname;
        this.nicknameHidden = false;
        this.skinHidden = false;
        this.fakeNickname = null;
    }

    public boolean isNicknameHidden() {
        return nicknameHidden;
    }

    public void setNicknameHidden(boolean nicknameHidden) {
        this.nicknameHidden = nicknameHidden;
    }

    public boolean isSkinHidden() {
        return skinHidden;
    }

    public void setSkinHidden(boolean skinHidden) {
        this.skinHidden = skinHidden;
    }

    public String getOriginalNickname() {
        return originalNickname;
    }

    public String getFakeNickname() {
        return fakeNickname;
    }

    public void setFakeNickname(String fakeNickname) {
        this.fakeNickname = fakeNickname;
    }

    public String getDisplayNickname() {
        return nicknameHidden && fakeNickname != null ? fakeNickname : originalNickname;
    }
}
