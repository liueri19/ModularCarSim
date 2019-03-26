package simulation;

import javafx.scene.shape.Polyline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The Track holds the edges which bound the Car.
 */
final class Track {
	/** Simple data class to represent a point in 2D space. */
	private static final class Point {
		double x, y;

		private Point(final double x, final double y) {
			this.x = x; this.y = y;
		}
	}


	/**
	 * The builder is able to track a path in a 2D space. The edges of the resulting shape
	 * bounds the Track.
	 */
	static final class TrackBuilder {
		private final List<Point> points = new ArrayList<>();

		void blockTo(final double x, final double y) {
			points.add(new Point(x, y));
		}

		Track build() {
			return new Track(points);
		}
	}


	/**
	 * Loads track from the specified file.
	 * @param filename  path to the track file
	 * @return  a new Track defined as in the specified file
	 */
	static Track load(final String filename) {
		try {
			// read file
			final List<String> lines = Files.readAllLines(Paths.get(filename));

			final TrackBuilder builder = new TrackBuilder();

			// for each line
			for (int i = 0; i < lines.size(); i++) {
				final String line = lines.get(i);
				if (line.isBlank()) continue;   // ignore blank lines

				// split x and y
				final var parts = line.split("\\h*");
				if (parts.length != 2) {
					throw new IllegalArgumentException(
							"Bad entry on line " + i + "in file '" + filename + "'");
				}

				// add entry to Track
				builder.blockTo(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
			}

			return builder.build();
		}
		catch (IOException e) {
			System.err.println("IOException occurred reading file '" + filename + "'");
			e.printStackTrace();
		}

		throw new IllegalStateException("Failed to build Track");
	}


	private final List<Obstacle> edges = new ArrayList<>();
	private final Polyline polyline = new Polyline();
	Polyline asPolyline() { return polyline; }

	private Track(final List<Point> points) {
		if (points.size() % 2 != 0)
			throw new IllegalArgumentException("points must have even number of elements");

		// setup polyline
		final List<Double> trackPoints = polyline.getPoints();
		points.forEach(point -> {
			trackPoints.add(point.x);
			trackPoints.add(point.y);
		});

		// set up edges
		for (int i = 1; i < points.size(); i++) {
			// connect all points into edges
			final Point p1, p2;
			p1 = points.get(i-1);
			p2 = points.get(i);

			edges.add(new Obstacle(p1.x, p1.y, p2.x, p2.y));
		}
	}


	List<Obstacle> getEdges() { return edges; }
}



/**
 * An Obstacle is a line that can block a range finder.
 * @see Car#getRangeReadingsAsList(Collection)
 */
final class Obstacle {
	private final double x1, y1, x2, y2;

	/**
	 * Constructs an Obstacle blocking a line from (x1, y1) to (x2, y2).
	 */
	Obstacle(final double x1, final double y1,
	         final double x2, final double y2) {
		this.x1 = x1;   this.y1 = y1;
		this.x2 = x2;   this.y2 = y2;
	}

	double getX1() {
		return x1;
	}

	double getY1() {
		return y1;
	}

	double getX2() {
		return x2;
	}

	double getY2() {
		return y2;
	}
}
