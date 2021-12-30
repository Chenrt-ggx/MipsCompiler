package Global;

public class AvlTree<T extends Comparable<T>> {
    private static final class TreeNode<T extends Comparable<T>> {
        private final T data;
        private int height;
        private int nodeSize;
        private int repeatTime;
        private TreeNode<T> leftSon;
        private TreeNode<T> rightSon;

        private TreeNode(T key) {
            data = key;
            leftSon = rightSon = null;
            nodeSize = repeatTime = 1;
        }
    }

    private TreeNode<T> root;

    public AvlTree() {
        root = null;
    }

    private int getSize(TreeNode<T> in) {
        if (in == null) {
            return 0;
        }
        return in.nodeSize;
    }

    private int getHeight(TreeNode<T> in) {
        if (in == null) {
            return 0;
        }
        return in.height;
    }

    private void pushUp(TreeNode<T> in) {
        if (in == null) {
            return;
        }
        in.nodeSize = in.repeatTime;
        in.nodeSize += getSize(in.leftSon);
        in.nodeSize += getSize(in.rightSon);
        in.height = Math.max(getHeight(in.leftSon), getHeight(in.rightSon)) + 1;
    }

    private TreeNode<T> getMaxNode(TreeNode<T> in) {
        TreeNode<T> now = in;
        if (now == null) {
            return null;
        }
        while (now.rightSon != null) {
            now = now.rightSon;
        }
        return now;
    }

    private TreeNode<T> leftRotate(TreeNode<T> in) {
        if (in == null) {
            return null;
        }
        TreeNode<T> tmp = in.rightSon;
        in.rightSon = tmp.leftSon;
        tmp.leftSon = in;
        tmp.nodeSize = getSize(in);
        pushUp(in);
        pushUp(tmp);
        return tmp;
    }

    private TreeNode<T> rightRotate(TreeNode<T> in) {
        if (in == null) {
            return null;
        }
        TreeNode<T> tmp = in.leftSon;
        in.leftSon = tmp.rightSon;
        tmp.rightSon = in;
        tmp.nodeSize = getSize(in);
        pushUp(in);
        pushUp(tmp);
        return tmp;
    }

    private TreeNode<T> maintain(TreeNode<T> in) {
        TreeNode<T> now = in;
        if (now == null) {
            return null;
        }
        if (getHeight(now.leftSon) - getHeight(now.rightSon) == 2) {
            if (getHeight(now.leftSon.leftSon) <= getHeight(now.leftSon.rightSon)) {
                now.leftSon = leftRotate(now.leftSon);
            }
            now = rightRotate(now);
        }
        if (getHeight(now.leftSon) - getHeight(now.rightSon) == -2) {
            if (getHeight(now.rightSon.rightSon) <= getHeight(now.rightSon.leftSon)) {
                now.rightSon = rightRotate(now.rightSon);
            }
            now = leftRotate(now);
        }
        return now;
    }

    private TreeNode<T> insert(TreeNode<T> in, T key) {
        TreeNode<T> now = in;
        if (now == null) {
            now = new TreeNode<>(key);
        }
        else if (now.data.compareTo(key) > 0) {
            now.leftSon = insert(now.leftSon, key);
            pushUp(now);
            now = maintain(now);
        }
        else if (now.data.compareTo(key) < 0) {
            now.rightSon = insert(now.rightSon, key);
            pushUp(now);
            now = maintain(now);
        }
        else {
            now.nodeSize++;
            now.repeatTime++;
        }
        return now;
    }

    public void insert(T key) {
        root = insert(root, key);
    }

    private TreeNode<T> upperNode(TreeNode<T> in, T key) {
        TreeNode<T> now = in;
        TreeNode<T> ans = getMaxNode(now);
        if (ans == null || ans.data.compareTo(key) < 0) {
            return null;
        }
        while (now != null) {
            if (now.data.compareTo(key) > 0) {
                if (now.data.compareTo(ans.data) < 0) {
                    ans = now;
                }
                now = now.leftSon;
            }
            else {
                now = now.rightSon;
            }
        }
        return ans;
    }

    public T upperBound(T key) {
        TreeNode<T> tmp = upperNode(root, key);
        if (tmp == null) {
            return null;
        }
        return tmp.data;
    }
}