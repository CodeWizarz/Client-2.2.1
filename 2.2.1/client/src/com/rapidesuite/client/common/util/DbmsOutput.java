/**************************************************
 * $Revision: 42178 $:
 * $Author: john.snell $:
 * $Date: 2014-07-11 16:35:43 +0700 (Fri, 11 Jul 2014) $:
 * $HeadURL: https://svn03.rapid4cloud.com/svn/a/dev/rapidesuite/programs/2.2.1/client/src/com/rapidesuite/client/common/util/DbmsOutput.java $:
 * $Id: DbmsOutput.java 42178 2014-07-11 09:35:43Z john.snell $:
 */

package com.rapidesuite.client.common.util;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class DbmsOutput
{
	/*
	 * our instance variables. It is always best to
	 * use callable or prepared statements and prepare (parse)
	 * them once per program execution, rather then one per
	 * execution in the program.  The cost of reparsing is
	 * very high.  Also -- make sure to use BIND VARIABLES!
	 *
	 * we use three statments in this class. One to enable
	 * dbms_output - equivalent to SET SERVEROUTPUT on in SQL*PLUS.
	 * another to disable it -- like SET SERVEROUTPUT OFF.
	 * the last is to "dump" or display the results from dbms_output
	 * using system.out
	 *
	 */
	private CallableStatement enable_stmt;
	private CallableStatement disable_stmt;
	private CallableStatement show_stmt;


	/*
	 * our constructor simply prepares the three
	 * statements we plan on executing.
	 *
	 * the statement we prepare for SHOW is a block of
	 * code to return a String of dbms_output output.  Normally,
	 * you might bind to a PLSQL table type but the jdbc drivers
	 * don't support PLSQL table types -- hence we get the output
	 * and concatenate it into a string.  We will retrieve at least
	 * one line of output -- so we may exceed your MAXBYTES parameter
	 * below. If you set MAXBYTES to 10 and the first line is 100
	 * bytes long, you will get the 100 bytes.  MAXBYTES will stop us
	 * from getting yet another line but it will not chunk up a line.
	 *
	 */
	public DbmsOutput( Connection conn ) throws SQLException
	{
		enable_stmt  = conn.prepareCall( "begin dbms_output.enable(:1); end;" );
		disable_stmt = conn.prepareCall( "begin dbms_output.disable; end;" );

		show_stmt = conn.prepareCall(
				"declare " +
				"    l_line varchar2(255); " +
				"    l_done number; " +
				"    l_buffer long; " +
				"begin " +
				"  loop " +
				"    exit when length(l_buffer)+255 > :maxbytes OR l_done = 1; " +
				"    dbms_output.get_line( l_line, l_done ); " +
				"    l_buffer := l_buffer || l_line || chr(10); " +
				"  end loop; " +
				" :done := l_done; " +
				" :buffer := l_buffer; " +
		"end;" );
	}

	/*
	 * enable simply sets your size and executes
	 * the dbms_output.enable call
	 *
	 */
	public void enable( int size ) throws SQLException
	{
		enable_stmt.setInt( 1, size );
		enable_stmt.executeUpdate();
	}

	/*
	 * disable only has to execute the dbms_output.disable call
	 */
	public void disable() throws SQLException
	{
		disable_stmt.executeUpdate();
	}

	/*
	 * show does most of the work.  It loops over
	 * all of the dbms_output data, fetching it in this
	 * case 32,000 bytes at a time (give or take 255 bytes).
	 * It will print this output on stdout by default (just
	 * reset what System.out is to change or redirect this
	 * output).
	 */

	public void show() throws SQLException
	{
		int  done = 0;
		show_stmt.registerOutParameter( 2, java.sql.Types.INTEGER );
		show_stmt.registerOutParameter( 3, java.sql.Types.VARCHAR );
		for(;;)
		{
			show_stmt.setInt( 1, 32000 );
			show_stmt.executeUpdate();
			String str = show_stmt.getString(3);
			if ( str != null && str.trim().length() > 0 )
			{
	            FileUtils.println( str );
			}
			if ( (done = show_stmt.getInt(2)) == 1 ) break;
		}
	}

	/*
	 * close closes the callable statements associated with
	 * the DbmsOutput class. Call this if you allocate a DbmsOutput
	 * statement on the stack and it is going to go out of scope --
	 * just as you would with any callable statement, result set
	 * and so on.
	 */
	public void close() throws SQLException
	{
		enable_stmt.close();
		disable_stmt.close();
		show_stmt.close();
	}

}