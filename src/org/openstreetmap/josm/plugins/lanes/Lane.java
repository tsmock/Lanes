package org.openstreetmap.josm.plugins.lanes;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Lane extends RoadPiece {

    private String change = null;

    public Lane(int direction, int position, MapView mv, RoadSegmentRenderer parent) {
        super(direction, position, mv, parent);
    }

    @Override
    public double getWidth(boolean start) {
        String widthTag = getWidthTag(start);
        double width = -1;

        if (widthTag != null && widthTag.equals("")) widthTag = null;

        try {
            width = parseLaneWidth(widthTag);
        } catch (Exception e) {
            widthTag = null;
        }

        if (widthTag == null) {
            if (_direction == 0) {
                return Utils.WIDTH_LANES + Utils.RENDERING_WIDTH_DIVIDER;
            } else {
                return Utils.WIDTH_LANES - Utils.RENDERING_WIDTH_DIVIDER;
            }
        }

        return width + (_direction == 0 ? 1 : -1) * Utils.RENDERING_WIDTH_DIVIDER;
    }

    @Override
    void render(Graphics2D g) {
        renderAsphalt(g);
        if (_direction == 0) {
            renderRoadLine(g, _offsetStart, _offsetEnd, Utils.DividerType.CENTRE_LANE, true);
        }
        renderTurnMarkings(g);
    }

    public String getChange() {
        String output = "";

        // Try extracting the change value from tags.  If the tag format is unreadable, return position based change instead.
        try {
            if (_direction == 1) {
                if (_way.hasTag("change:lanes:forward")) {
                    output = splitPos(_way.get("change:lanes:forward"), _position);
                }
                if (_way.hasTag("change:lanes") && _way.isOneway() == 1) {
                    output = splitPos(_way.get("change:lanes"), _position);
                }
                if (_way.hasTag("change:forward")) {
                    output = _way.get("change:forward");
                }
                if (_way.hasTag("change")) {
                    output = _way.get("change");
                }
            } else if (_direction == -1) {
                if (_way.hasTag("change:lanes:backward")) {
                    output = splitPos(_way.get("change:lanes:backward"), _position);
                }
                if (_way.hasTag("change:backward")) {
                    output = _way.get("change:backward");
                }
                if (_way.hasTag("change")) {
                    output = _way.get("change");
                }
            }
        } catch (Exception ignored) {}

        // If the local change value has been overriden, fallback on that instead.
        if (change != null) output = change;

        // If the change value is invalid (aka "none", "", "no_tleft", etc, use the position based change value instead.
        if (output == null || !(output.equals("yes") || output.equals("no") || output.equals("not_left")
                || output.equals("not_right"))) output = getDefaultChange();

        return output;
    }

    public String getDefaultChange() {
        boolean left = _left != null && _left.defaultChangeToThis();
        boolean right = _right != null && _right.defaultChangeToThis();
        return left && right ? "yes" : left ? "not_right" : right ? "not_left" : "no";
    }

    public void setChange(String newChange) {
        if (newChange == null || !(newChange.equals("yes") || newChange.equals("no") || newChange.equals("not_left")
                || newChange.equals("not_right"))) return;

        change = newChange;
    }

    public String getTurn() {
        try {
            if (_direction == 1) {
                if (_way.hasTag("turn:lanes:forward")) {
                    return splitPos(_way.get("turn:lanes:forward"), _position);
                }
                if (_way.hasTag("turn:lanes") && _way.isOneway() == 1) {
                    return splitPos(_way.get("turn:lanes"), _position);
                }
                if (_way.hasTag("turn:forward")) {
                    return _way.get("turn:forward");
                }
                if (_way.hasTag("turn")) {
                    return _way.get("turn");
                }
            } else if (_direction == -1) {
                if (_way.hasTag("turn:lanes:backward")) {
                    return splitPos(_way.get("turn:lanes:backward"), _position);
                }
                if (_way.hasTag("turn:backward")) {
                    return _way.get("turn:backward");
                }
                if (_way.hasTag("turn")) {
                    return _way.get("turn");
                }
            } else if (_direction == 0) {
                if (_way.hasTag("turn:lanes:both_ways")) {
                    return splitPos(_way.get("turn:lanes:both_ways"), 0);
                }
                if (_way.hasTag("turn:both_ways")) {
                    return _way.get("turn:both_ways");
                }
                if (_way.hasTag("turn")) {
                    return _way.get("turn");
                }
            }
        } catch (Exception e) {}
        return null;
    }

    public String getWidthTag(boolean start) {
        String output = "";
        if (_direction == 1) {
            if (output.equals("") && start && _way.hasTag("width:lanes:forward:start")) {
                output = splitPos(_way.get("width:lanes:forward:start"), _position);
            }
            if (output.equals("") && !start && _way.hasTag("width:lanes:forward:end")) {
                output = splitPos(_way.get("width:lanes:forward:end"), _position);
            }
            if (output.equals("") && _way.hasTag("width:lanes:forward")) {
                output = splitPos(_way.get("width:lanes:forward"), _position);
            }

            if (output.equals("") && start && _way.hasTag("width:lanes:start") && _way.isOneway() == 1) {
                output = splitPos(_way.get("width:lanes:start"), _position);
            }
            if (output.equals("") && !start && _way.hasTag("width:lanes:end") && _way.isOneway() == 1) {
                output = splitPos(_way.get("width:lanes:end"), _position);
            }
            if (output.equals("") && _way.hasTag("width:lanes") && _way.isOneway() == 1) {
                output = splitPos(_way.get("width:lanes"), _position);
            }
        } else if (_direction == -1) {
            if (output.equals("") && start && _way.hasTag("width:lanes:backward:start")) {
                output = splitPos(_way.get("width:lanes:backward:start"), _position);
            }
            if (output.equals("") && !start && _way.hasTag("width:lanes:backward:end")) {
                output = splitPos(_way.get("width:lanes:backward:end"), _position);
            }
            if (output.equals("") && _way.hasTag("width:lanes:backward")) {
                output = splitPos(_way.get("width:lanes:backward"), _position);
            }
        } else if (_direction == 0) {
            if (output.equals("") && start && _way.hasTag("width:lanes:both_ways:start")) {
                output = splitPos(_way.get("width:lanes:both_ways:start"), _position);
            }
            if (output.equals("") && !start && _way.hasTag("width:lanes:both_ways:end")) {
                output = splitPos(_way.get("width:lanes:both_ways:end"), _position);
            }
            if (output.equals("") && _way.hasTag("width:lanes:both_ways")) {
                output = splitPos(_way.get("width:lanes:both_ways"), _position);
            }
        }
        return output.equals("") ? null : output;
    }

    private String splitPos(String str, int pos) {
        StringBuilder output = new StringBuilder();
        int bars = 0;
        for (char c : str.toCharArray()) {
            if (c == '|') {
                bars++;
            } else if (bars == pos) {
                output.append(c);
            } else if (bars > pos) {
                break;
            }
        }
        return output.toString();
    }

    private void renderTurnMarkings(Graphics2D g) {
        if (_mv.getScale() > 1.0) return; // Don't render turn lane markings when the map is zoomed out

        try {
            String turn = getTurn();

            if (turn == null) return;

            int numDrawn = 0;
            double distSoFar = 0;
            Way lanePos = Utils.getParallel(_parent.getAlignment(), _offsetStart, _offsetEnd, false);
            for (int i = 0; i < lanePos.getNodesCount() - 1; i++) {
                double distThisTime = lanePos.getNode(i).getCoor().greatCircleDistance(lanePos.getNode(i + 1).getCoor());
                double angle = lanePos.getNode(i).getCoor().bearing(lanePos.getNode(i + 1).getCoor());
                if (_direction == -1) angle += Math.PI;

                if (_direction != 0 && distSoFar + distThisTime > Utils.DIST_TO_FIRST_TURN + Utils.DIST_BETWEEN_TURNS * (numDrawn)) {
                    double distIntoSegment = Utils.DIST_TO_FIRST_TURN + Utils.DIST_BETWEEN_TURNS * (numDrawn) - distSoFar;
                    double portionFirst = (distThisTime - distIntoSegment) / distThisTime;
                    LatLon pos = new LatLon(lanePos.getNode(i).lat() * portionFirst + (lanePos.getNode(i + 1).lat() * (1 - portionFirst)),
                            lanePos.getNode(i).lon() * portionFirst + (lanePos.getNode(i + 1).lon() * (1 - portionFirst)));
                    Point point = _mv.getPoint(pos);
                    double portionStart = (distSoFar + distIntoSegment) / _way.getLength();
                    double width = getWidth(false) * portionStart + getWidth(true) * (1 - portionStart);
                    drawTurnMarkingsAt(turn, g, point.x, point.y, width, angle);
                    distSoFar -= distThisTime;
                    i--;
                    numDrawn++;
                }
                if (_direction == 0 && distSoFar + distThisTime > Utils.DIST_TO_FIRST_TURN + Utils.DIST_BETWEEN_TURNS * (numDrawn) &&
                        distSoFar + distThisTime - Utils.DIST_TO_FIRST_TURN - (Utils.DIST_BETWEEN_TURNS * (numDrawn)) > 5) {
                    double distIntoSegment = Utils.DIST_TO_FIRST_TURN + Utils.DIST_BETWEEN_TURNS * (numDrawn) - distSoFar;
                    double portionFirst = (distThisTime - distIntoSegment) / distThisTime;
                    double portionStart = (distSoFar + distIntoSegment) / _way.getLength();
                    double width = getWidth(false) * portionStart + getWidth(true) * (1 - portionStart) - Utils.RENDERING_WIDTH_DIVIDER * 2;
                    LatLon pos = new LatLon(lanePos.getNode(i).lat() * portionFirst + (lanePos.getNode(i + 1).lat() * (1 - portionFirst)),
                            lanePos.getNode(i).lon() * portionFirst + (lanePos.getNode(i + 1).lon() * (1 - portionFirst)));
                    LatLon posBack = Utils.getLatLonRelative(pos, angle + Math.PI, 0.67 * width);
                    LatLon posFront = Utils.getLatLonRelative(pos, angle, 0.67 * width);
                    Point pointBack = _mv.getPoint(posBack);
                    Point pointFront = _mv.getPoint(posFront);
                    drawTurnMarkingsAt(turn, g, pointBack.x, pointBack.y, width, angle);
                    drawTurnMarkingsAt(turn, g, pointFront.x, pointFront.y, width, angle + Math.PI);
                    distSoFar -= distThisTime;
                    i--;
                    numDrawn++;
                }
                distSoFar += distThisTime;
            }
        } catch (Exception ignored) {} // Just don't render the turn markings if they can't be rendered.
    }

    private void drawTurnMarkingsAt(String turn, Graphics2D g, int x, int y, double width, double rotationRadians) {
        // Ensure that this road marking is within 30 ft of the map before rendering.
        BBox bBox = _mv.getRealBounds().toBBox();
        if (((x < _mv.getPoint(bBox.getTopLeft()).x) || (x > _mv.getPoint(bBox.getBottomRight()).x)) &&
                ((y < _mv.getPoint(bBox.getTopLeft()).y) || (y > _mv.getPoint(bBox.getBottomRight()).y))) return;

        double rotationModPiOverTwo = rotationRadians % (Math.PI / 2);
        width = width * (Math.cos(rotationModPiOverTwo) + Math.sin(rotationModPiOverTwo));

        int offset = (int) (width * 50 / _mv.getDist100Pixel());
        x -= offset;
        y -= offset;

        List<String> turns = new ArrayList<>();
        Collections.addAll(turns, turn.split(";"));
        boolean lr = _mv.getScale() > 0.04;
        if (turns.contains("left")) drawImageAt(g, lr ? Utils.lr_left : Utils.left, x, y, width, rotationRadians);
        if (turns.contains("right")) drawImageAt(g, lr ? Utils.lr_right : Utils.right, x, y, width, rotationRadians);
        if (turns.contains("slight_left")) drawImageAt(g, lr ? Utils.lr_slightLeft : Utils.slightLeft, x, y, width, rotationRadians);
        if (turns.contains("slight_right")) drawImageAt(g, lr ? Utils.lr_slightRight : Utils.slightRight, x, y, width, rotationRadians);
        if (turns.contains("through")) drawImageAt(g, lr ? Utils.lr_through : Utils.through, x, y, width, rotationRadians);
        if (turns.contains("merge_to_left")) drawImageAt(g, lr ? Utils.lr_mergeLeft : Utils.mergeLeft, x, y, width, rotationRadians);
        if (turns.contains("merge_to_right")) drawImageAt(g, lr ? Utils.lr_mergeRight : Utils.mergeRight, x, y, width, rotationRadians);
        if (turns.contains("reverse")) drawImageAt(g, Utils.isRightHand(_way) ? (lr ? Utils.lr_uTurnLeft : Utils.uTurnLeft) :
                (lr ? Utils.lr_uTurnRight : Utils.uTurnRight), x, y, width, rotationRadians);
    }

    private void drawImageAt(Graphics2D g, Image image, int x, int y, double width, double rotationRadians) {
        int size = (int) (width * 100 / _mv.getDist100Pixel()) + 1;
        g.drawImage(rotate(toBufferedImage(image), rotationRadians), x, y, size, size, null);
    }

    public BufferedImage rotate(BufferedImage image, double angle) {
        double sin = Math.abs(Math.sin(angle));
        double cos = Math.abs(Math.cos(angle));
        int w = image.getWidth();
        int h = image.getHeight();
        int newWidth = (int)Math.floor(w*cos+h*sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        GraphicsConfiguration gc = getDefaultConfiguration();
        BufferedImage result = gc.createCompatibleImage(newWidth, newHeight, Transparency.TRANSLUCENT);
        Graphics2D g = result.createGraphics();

        g.translate((newWidth - w) / 2, (newHeight - h) / 2);
        g.rotate(angle, w / 2, h / 2);
        g.drawRenderedImage(image, null);
        g.dispose();

        return result;
    }

    private GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }

    public BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) return (BufferedImage) img;

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}