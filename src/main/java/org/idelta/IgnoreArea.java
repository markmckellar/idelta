package org.idelta;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;

public class IgnoreArea
{
	private Rectangle2D.Double ignoreRectangle;
	private Rectangle2D.Double beforeResizingRectangle;
	private boolean selected;
	private CornerType selectedCornerType;



	public enum CornerType
	{
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
	}

	public IgnoreArea(Rectangle2D.Double ignoreRectangle)
	{
		super();
		setIgnoreRectangle(ignoreRectangle);
		this.setSelected(false);
	}

	public Rectangle2D.Double getMappedIgnoreRectangle(Rectangle2D.Double ingoreRectangle,double mappedWidth,double mappedHeight)
	{
		double x = ingoreRectangle.getX()*mappedWidth;
		double y = ingoreRectangle.getY()*mappedHeight;
		double width = ingoreRectangle.getWidth()*mappedWidth;
		double height = ingoreRectangle.getHeight()*mappedHeight;
		Rectangle2D.Double mappedIgnoreRectangle = new Rectangle2D.Double(x,y,width,height);
		return(mappedIgnoreRectangle);		
	}
	
	public Rectangle2D.Double getReversedMappedIgnoreRectangle(Rectangle2D.Double ingoreRectangle,double mappedWidth,double mappedHeight)
	{
		double x = ingoreRectangle.getX()/mappedWidth;
		double y = ingoreRectangle.getY()/mappedHeight;
		double width = ingoreRectangle.getWidth()/mappedWidth;
		double height = ingoreRectangle.getHeight()/mappedHeight;
		Rectangle2D.Double mappedIgnoreRectangle = new Rectangle2D.Double(x,y,width,height);
		return(mappedIgnoreRectangle);		
	}
	
	public Rectangle2D.Double getIgnoreRectangle()
	{
		return ignoreRectangle;
	}

	public void setIgnoreRectangle(Rectangle2D.Double ignoreRectangle)
	{
		this.ignoreRectangle = ignoreRectangle;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public CornerType getConerTypeClicked(Point2D point,double mappedWidth,double mappedHeight)
	{
		CornerType whichCorner = null;
		for(ResizeableRectangle rectangle:getResizeRectagleList(mappedWidth, mappedHeight))
		{
			if(rectangle.contains(point))
			{
				whichCorner = rectangle.getCornerType();
				break;
			}
		}
		return(whichCorner);
	}

	public List<ResizeableRectangle> getResizeRectagleList(double mappedWidth,double mappedHeight)
	{
		List<ResizeableRectangle> resizeRectagleList = new ArrayList<ResizeableRectangle>();
		Rectangle2D.Double mappedRectangle = getMappedIgnoreRectangle(getIgnoreRectangle(),mappedWidth, mappedHeight);
		double width = 50.0;
		// top left
		resizeRectagleList.add(new ResizeableRectangle(mappedRectangle.getX() - width, mappedRectangle.getY() - width, width, width, CornerType.TOP_LEFT));
		// bottom left
		resizeRectagleList.add(new ResizeableRectangle(mappedRectangle.getX() - width, mappedRectangle.getY() + mappedRectangle.getHeight(), width, width, CornerType.BOTTOM_LEFT));
		// top right
		resizeRectagleList.add(new ResizeableRectangle(mappedRectangle.getX() + mappedRectangle.getWidth(), mappedRectangle.getY() - width, width, width, CornerType.TOP_RIGHT));
		// bottom right
		resizeRectagleList.add(new ResizeableRectangle(mappedRectangle.getX() + mappedRectangle.getWidth(), mappedRectangle.getY() + mappedRectangle.getHeight(), width, width,
				CornerType.BOTTOM_RIGHT));

		return(resizeRectagleList);
	}

	public void resizeRectangle(Point2D mouseClickPoint, Point2D mouseDragPoint,double mappedWidth,double mappedHeight)
	{
		double deltaX = mouseDragPoint.getX() - mouseClickPoint.getX();
		double deltaY = mouseDragPoint.getY() - mouseClickPoint.getY();
		Rectangle2D.Double mappedBeforeRectangle = getMappedIgnoreRectangle(getBeforeResizingRectangle(),mappedWidth, mappedHeight);
		
		Point2D startPoint = getResizeRectangleStart(mappedBeforeRectangle, getSelectedCornerType());
		Point2D endPoint = getResizeRectangleEnd(mappedBeforeRectangle, getSelectedCornerType(), deltaX, deltaY);

		if(startPoint.getX() > endPoint.getX())
		{
			System.out.println("START resizeRectangle:swap X");
			double holdX = startPoint.getX();
			startPoint = new Point2D.Double(endPoint.getX(), startPoint.getY());
			endPoint = new Point2D.Double(holdX, endPoint.getY());
		}
		if(startPoint.getY() > endPoint.getY())
		{
			System.out.println("START resizeRectangle:swap Y");
			double holdY = startPoint.getY();
			startPoint = new Point2D.Double(startPoint.getX(), endPoint.getY());
			endPoint = new Point2D.Double(endPoint.getX(), holdY);
		}
		Rectangle2D.Double newRectangle = this.getReversedMappedIgnoreRectangle(new Rectangle2D.Double(startPoint.getX(), startPoint.getY(), endPoint.getX() - startPoint.getX(), endPoint.getY() - startPoint.getY()), mappedWidth, mappedHeight);
		this.setIgnoreRectangle(newRectangle);
	}

	public void moveRectangle(Point2D mouseClickPoint, Point2D mouseDragPoint,double mappedWidth,double mappedHeight)
	{
		double deltaX = mouseDragPoint.getX() - mouseClickPoint.getX();
		double deltaY = mouseDragPoint.getY() - mouseClickPoint.getY();
		Rectangle2D.Double mappedBeforeRectangle = getMappedIgnoreRectangle(getBeforeResizingRectangle(),mappedWidth, mappedHeight);

		
		Rectangle2D.Double newRectangle = this.getReversedMappedIgnoreRectangle(new Rectangle2D.Double(mappedBeforeRectangle.getX() + deltaX, mappedBeforeRectangle.getY() + deltaY, mappedBeforeRectangle.getWidth(),
				mappedBeforeRectangle.getHeight()), mappedWidth, mappedHeight);
		setIgnoreRectangle(newRectangle);
	}

	public Point2D getResizeRectangleEnd(Rectangle2D.Double startRectangle, CornerType cornerType, double deltaX, double deltaY)
	{
		Point2D start = null;
		if(cornerType == CornerType.TOP_LEFT)
			start = new Point2D.Double(startRectangle.getX() + deltaX, startRectangle.getY() + deltaY);
		else if(cornerType == CornerType.BOTTOM_RIGHT)
			start = new Point2D.Double(startRectangle.getX() + startRectangle.getWidth() + deltaX, startRectangle.getY() + startRectangle.getHeight() + deltaY);
		else if(cornerType == CornerType.TOP_RIGHT)
			start = new Point2D.Double(startRectangle.getX() + startRectangle.getWidth() + deltaX, startRectangle.getY() + deltaY);
		else if(cornerType == CornerType.BOTTOM_LEFT) start = new Point2D.Double(startRectangle.getX() + deltaX, startRectangle.getY() + startRectangle.getHeight() + deltaY);
		return(start);
	}

	public Point2D getResizeRectangleStart(Rectangle2D.Double startRectangle, CornerType cornerType)
	{
		Point2D start = null;
		if(cornerType == CornerType.TOP_LEFT)
			start = new Point2D.Double(startRectangle.getX() + startRectangle.getWidth(), startRectangle.getY() + startRectangle.getHeight());
		else if(cornerType == CornerType.BOTTOM_RIGHT)
			start = new Point2D.Double(startRectangle.getX(), startRectangle.getY());
		else if(cornerType == CornerType.TOP_RIGHT)
			start = new Point2D.Double(startRectangle.getX(), startRectangle.getY() + startRectangle.getHeight());
		else if(cornerType == CornerType.BOTTOM_LEFT) start = new Point2D.Double(startRectangle.getX() + startRectangle.getWidth(), startRectangle.getY());
		return(start);
	}

	public void drawFxIgnoreAreas(GraphicsContext gc, Point2D mouseClickPoint, Point2D mouseDragPoint, boolean isMouseDragging,double mappedWidth,double mappedHeight)
	{
		javafx.scene.paint.Color baseColor = javafx.scene.paint.Color.RED;
		if(this.isSelected()) baseColor = javafx.scene.paint.Color.GREEN;
		Rectangle2D.Double mappedRectangle = getMappedIgnoreRectangle(getIgnoreRectangle(),mappedWidth, mappedHeight);

		javafx.scene.paint.Color outlineColor = new javafx.scene.paint.Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 1.0);
		javafx.scene.paint.Color fillColor = new javafx.scene.paint.Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0.2);

		if(this.isSelected())
		{
			if(this.getSelectedCornerType() != null)
				this.resizeRectangle(mouseClickPoint, mouseDragPoint,mappedWidth,mappedHeight);
			else if(isMouseDragging) moveRectangle(mouseClickPoint, mouseDragPoint,mappedWidth,mappedHeight);

			for(ResizeableRectangle rectangle:getResizeRectagleList(mappedWidth,mappedHeight))
			{
				drawFxRect(gc, rectangle, outlineColor, fillColor);
			}
		}
		drawFxRect(gc, mappedRectangle, outlineColor, fillColor);
	}

	public void drawFxRect(GraphicsContext gc, Rectangle2D.Double rectangle, javafx.scene.paint.Color outlineColor, javafx.scene.paint.Color fillColor)
	{
		gc.setStroke(outlineColor);
		gc.setLineWidth(2);
		gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
		gc.setFill(fillColor);
		gc.fillRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());

	}

	public CornerType getSelectedCornerType()
	{
		return selectedCornerType;
	}

	public void setSelectedCornerType(CornerType selectedCornerType)
	{
		this.selectedCornerType = selectedCornerType;
	}

	public Rectangle2D.Double getBeforeResizingRectangle()
	{
		return beforeResizingRectangle;
	}

	public void setBeforeResizingRectangle()
	{
		this.setBeforeResizingRectangle(new Rectangle2D.Double(getIgnoreRectangle().getX(), getIgnoreRectangle().getY(), getIgnoreRectangle().getWidth(), getIgnoreRectangle().getHeight()));
	}

	public void setBeforeResizingRectangle(Rectangle2D.Double beforeResizingRectangle)
	{
		this.beforeResizingRectangle = beforeResizingRectangle;
	}

	public class ResizeableRectangle extends Rectangle2D.Double
	{
		private IgnoreArea.CornerType cornerType;

		public ResizeableRectangle(double x, double y, double width, double hieght, IgnoreArea.CornerType cornerType)
		{
			super(x, y, width, hieght);
			this.setCornerType(cornerType);
		}

		public IgnoreArea.CornerType getCornerType()
		{
			return cornerType;
		}

		public void setCornerType(IgnoreArea.CornerType cornerType)
		{
			this.cornerType = cornerType;
		}

	}

}
