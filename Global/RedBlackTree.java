package Global;

import java.util.NoSuchElementException;

public class RedBlackTree<T extends Comparable<T>> {
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private static final class Node<T> {
        private int size;
        private int repeat;
        private final T data;
        private boolean color;

        private Node<T> father;
        private Node<T> leftSon;
        private Node<T> rightSon;

        private Node(T data, Node<T> father) {
            this.data = data;
            this.color = BLACK;
            this.father = father;
            this.size = this.repeat = 1;
            this.leftSon = this.rightSon = null;
        }

        private void pushUp() {
            size = repeat;
            if (leftSon != null) {
                size += leftSon.size;
            }
            if (rightSon != null) {
                size += rightSon.size;
            }
        }
    }

    private Node<T> root;

    private Node<T> parentOf(Node<T> current) {
        if (current == null) {
            return null;
        }
        return current.father;
    }

    private Node<T> leftOf(Node<T> current) {
        if (current == null) {
            return null;
        }
        return current.leftSon;
    }

    private Node<T> rightOf(Node<T> current) {
        if (current == null) {
            return null;
        }
        return current.rightSon;
    }

    private boolean getColor(Node<T> current) {
        if (current == null) {
            return BLACK;
        }
        return current.color;
    }

    private void setColor(Node<T> current, boolean color) {
        if (current != null) {
            current.color = color;
        }
    }

    private Node<T> getMaxNode(Node<T> current) throws NoSuchElementException {
        Node<T> now = current;
        if (now == null) {
            throw new NoSuchElementException();
        }
        while (now.rightSon != null) {
            now = now.rightSon;
        }
        return now;
    }

    private void relink(Node<T> current, Node<T> relink) {
        if (relink != null) {
            relink.father = current.father;
        }
        if (current.father == null) {
            root = relink;
        }
        else if (current == current.father.leftSon) {
            current.father.leftSon = relink;
        }
        else {
            current.father.rightSon = relink;
        }
    }

    private void leftRotate(Node<T> current) {
        if (current != null) {
            Node<T> node = current.rightSon;
            current.rightSon = node.leftSon;
            if (node.leftSon != null) {
                node.leftSon.father = current;
            }
            relink(current, node);
            node.leftSon = current;
            current.father = node;
            current.pushUp();
            node.pushUp();
        }
    }

    private void rightRotate(Node<T> current) {
        if (current != null) {
            Node<T> node = current.leftSon;
            current.leftSon = node.rightSon;
            if (node.rightSon != null) {
                node.rightSon.father = current;
            }
            relink(current, node);
            node.rightSon = current;
            current.father = node;
            current.pushUp();
            node.pushUp();
        }
    }

    public RedBlackTree() {
        root = null;
    }

    private void fixInsert(Node<T> current) {
        Node<T> node = current;
        node.color = RED;
        while (node != null && node != root && node.father.color == RED) {
            if (parentOf(node) == leftOf(parentOf(parentOf(node)))) {
                Node<T> uncle = rightOf(parentOf(parentOf(node)));
                if (getColor(uncle) == RED) {
                    setColor(parentOf(node), BLACK);
                    setColor(uncle, BLACK);
                    setColor(parentOf(parentOf(node)), RED);
                    node = parentOf(parentOf(node));
                }
                else {
                    if (node == rightOf(parentOf(node))) {
                        node = parentOf(node);
                        leftRotate(node);
                    }
                    setColor(parentOf(node), BLACK);
                    setColor(parentOf(parentOf(node)), RED);
                    rightRotate(parentOf(parentOf(node)));
                }
            }
            else {
                Node<T> uncle = leftOf(parentOf(parentOf(node)));
                if (getColor(uncle) == RED) {
                    setColor(parentOf(node), BLACK);
                    setColor(uncle, BLACK);
                    setColor(parentOf(parentOf(node)), RED);
                    node = parentOf(parentOf(node));
                }
                else {
                    if (node == leftOf(parentOf(node))) {
                        node = parentOf(node);
                        rightRotate(node);
                    }
                    setColor(parentOf(node), BLACK);
                    setColor(parentOf(parentOf(node)), RED);
                    leftRotate(parentOf(parentOf(node)));
                }
            }
        }
        root.color = BLACK;
    }

    public void insert(T data) {
        Node<T> father = null;
        Node<T> current = root;
        while (current != null) {
            father = current;
            father.size++;
            if (data.compareTo(current.data) < 0) {
                current = current.leftSon;
            }
            else if (data.compareTo(current.data) > 0) {
                current = current.rightSon;
            }
            else {
                current.repeat++;
                return;
            }
        }
        current = new Node<>(data, father);
        if (father == null) {
            root = current;
        }
        else if (data.compareTo(father.data) < 0) {
            father.leftSon = current;
        }
        else {
            father.rightSon = current;
        }
        fixInsert(current);
    }

    public T upperBound(T data) throws NoSuchElementException {
        Node<T> current = root;
        Node<T> node = getMaxNode(root);
        if (data.compareTo(node.data) >= 0) {
            throw new NoSuchElementException();
        }
        while (current != null) {
            if (data.compareTo(current.data) < 0) {
                if (node.data.compareTo(current.data) > 0) {
                    node = current;
                }
                current = current.leftSon;
            }
            else {
                current = current.rightSon;
            }
        }
        return node.data;
    }
}