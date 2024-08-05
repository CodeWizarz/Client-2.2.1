package com.rapidesuite.build.gui.apigrid;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.rapidesuite.client.common.gui.datagrid.DataGridDataLoadThread;
import com.rapidesuite.client.common.util.FileUtils;
import com.rapidesuite.client.common.util.GUIUtils;
import com.rapidesuite.core.utility.WorkbookUtil;

public class APIDataGridLoadExcelThread extends DataGridDataLoadThread
{

	private List<String[]> excelDataRows;
	private List<String> inventoryColumnNames;

	public APIDataGridLoadExcelThread(InputStream inputStream, List<String> inventoryColumnNames)
	{
		super(inputStream);
		this.inventoryColumnNames = inventoryColumnNames;
	}

	public void start() throws Exception
	{
		new Thread(this).start();
	}

	public void run()
	{
		try
		{
			excelDataRows = getRowsFromExcelDataFile(inputStream, inventoryColumnNames);
			isProcessingCompleted = true;
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to load the data file. Error: " + e.getMessage());
		}
		finally
		{
			IOUtils.closeQuietly(inputStream);
		}
	}

	private List<String[]> getRowsFromExcelDataFile(InputStream inputStream, List<String> columnNames)
	{
		final List<String[]> toReturn = new ArrayList<String[]>();
		try
		{
			HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
			HSSFSheet sheet = workbook.getSheetAt(0);
			int recordsCount = 0;
			final int totalRows = sheet.getPhysicalNumberOfRows() - 1;

			// Parsing the header columns
			HSSFRow headerRow = sheet.getRow(0);
			Map<String, Integer> headerColumnNamesToPositionMap = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
			for ( int k = 0; k <= headerRow.getLastCellNum(); k++ )
			{
				String columnName = WorkbookUtil.getCellContentsAsString(headerRow.getCell(k));
				if ( columnName.isEmpty() )
				{
					break;
				}
				headerColumnNamesToPositionMap.put(columnName, k);
			}

			// We discard the header
			for ( int j = 1; j <= totalRows; j++ )
			{
				final String[] row = new String[columnNames.size()];
				final HSSFRow rowExcel = workbook.getSheetAt(0).getRow(j);
				if ( rowExcel == null )
				{
					continue;
				}
				boolean skip = true;
				for ( int k = 0; k < columnNames.size(); k++ )
				{
					String columnName = columnNames.get(k);
					Integer position = headerColumnNamesToPositionMap.get(columnName);
					if ( position != null )
					{
						String columnValue = WorkbookUtil.getCellContentsAsString(rowExcel.getCell(position.intValue()));
						if ( columnValue != null && !columnValue.trim().equals("") )
						{
							skip = false;
						}
						row[k] = columnValue;
					}
				}
				if ( skip )
				{
					continue;
				}
				recordsCount++;
				toReturn.add(row);
			}
		}
		catch ( Exception e )
		{
			FileUtils.printStackTrace(e);
			GUIUtils.popupErrorMessage("Unable to import Excel file. Error message: " + e.getMessage());
		}
		return toReturn;
	}

	@Override
	public List<String[]> getDataRows()
	{
		return excelDataRows;
	}

}