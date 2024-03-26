<?php
 $offset = $_REQUEST['offset'];
 $pgnum = $_REQUEST['pgnum'];
 settype($offset, 'integer');
 settype($pgnum, 'integer');

// -----set beginning and ending extension number ------.
$lowext=1;
$highext=9999;

// ----- customize your logo-------.
//print "<img src=siptel.png>";


echo "<font face=verdana size=2><b>sipXcom Live CDR Project version 3.0</b></font><br>";
$client = new SoapClient(NULL, array('cache_wsdl' => WSDL_CACHE_NONE, 'location' => "http://127.0.0.1:8130", 'uri' => "urn:CdrService"));
try {
        $total = count($client->__soapCall("getActiveCalls", array(), array('soapaction' => "getActiveCalls")));
} catch (Exception $e) {
        print "0";
        print "\n";
        exit(1);
}
$list=$client->__soapCall("getActiveCalls", array(), array('soapaction' => "getActiveCalls"));
print "<br><font face=verdana size=1>Total calls : ".$total."\n";
$total=$total-1;
 If ($total >=0) {
  $stack=array();
  echo "<table border=1>";
  do {
	echo "<tr>";
	$from=$list[$total]->from;
	$duration=$list[$total]->duration;
	$recipient=$list[$total]->recipient;
	$start=$list[$total]->start_time;
	$to=$list[$total]->to;
	echo "<td><font face=verdana size=1:>". htmlspecialchars($from)."</td>";
	echo "<td><font face=verdana size=1:>". htmlspecialchars($to)."</td>";
	echo "<td><font face=verdana size=1:>". htmlspecialchars($recipient)."</td>";
	echo "<td><font face=verdana size=1:>".$start."</td>";
	echo "<td>";
	print "<font face=verdana size=1>Duration: " . floor(floor($duration / 1000)/60)." min.";
	echo "</td>";
	$total--;
	echo "</tr>";
	$mystring = htmlspecialchars($from);
	$findme   = ':';
	$pos = strpos($mystring, $findme);
	$mystring = htmlspecialchars($from);
	$findme   = '@';
	$pos2 = strpos($mystring, $findme);
	$rest = substr($mystring, $pos+1, $pos2-$pos-1);
	//echo $rest;
	array_push($stack, $rest);

	$mystring = htmlspecialchars($to);
	$findme   = ':';
	$pos = strpos($mystring, $findme);
	$mystring = htmlspecialchars($to);
	$findme   = '@';
	$pos2 = strpos($mystring, $findme);
	$rest = substr($mystring, $pos+1, $pos2-$pos-1);
	array_push($stack, $rest);

} while ($total>="");
echo "</table>";
}
elseif ($total<0){
print "<br><br>";
}
echo "<br><br><font face=verdana size=2><b>Extension list</b></font><br>";
echo "<br><table class='gradienttable' border=0><tr valign=top><td><table border=0>";
$i=0;
$nbappel=0;
$conn = pg_pconnect("dbname=SIPXCONFIG user=postgres");
 $limit=45; // rows to return
 $numresults=pg_query("select * from users order by user_name"); //
 $numrows=pg_num_rows($numresults);

 if (empty($offset)) {
 	$offset=0;
	 $pgnum=1;
 }
        // get results
$result1=pg_query("select * from users order by user_name limit $limit offset $offset"); // PostgreSQL
while ($row = pg_fetch_row($result1)) {
  $extension=($row[5]);
  if ("$extension">"$lowext") 
  {
    if ("$extension"<"$highext") 
    {

	$image="phone.png";
	$classred="";
	if (in_array($row[5], $stack)) {
	    $image="phone2.png";
	    $classred=red;
	}
	$color1="<font size=2 color=white><b>";
	 print "<tr><td height=45 width=240 class=$classred><font face=verdana size=2 color=white>$color1<img src=$image>";
	 print htmlspecialchars($row[5]);
	 print "<span title='$inout $text1 $text2'> <font face=verdana size=2 color=white>$color1";
	 print htmlspecialchars($row[1]);
	 print "</span> <font face=verdana size=2 color=white>$color1";
	 print htmlspecialchars($row[4]);
	 $i++;

	if ($i==10)
	{
		print "</table></td><td><table border=0>";
		$i=0;
	}
    }
  }
}
print "</table></td><td><table border=0>";
$queuecdr=shell_exec("/opt/freeswitch/bin/fs_cli -x 'show detailed_calls' | grep callcenter | awk -F \",\" '{print $10}'");
$confcdr=shell_exec("/opt/freeswitch/bin/fs_cli -x 'show detailed_calls' | grep conference | awk -F \",\" '{print $10}'");
print "</table>";
echo "<br><br><font color=white face=verdana size=2><b>Queues</b></font><br><table border=0><td height=45 width=240>&nbsp;<font color=white face=verdana size=2>$queuecdr</td></table>";
echo "<br><br><font color=white face=verdana size=2><b>Conferences</b></font><br><table border=0><td height=45 width=240>&nbsp;<font color=white face=verdana size=2>$confcdr</td></table>";
echo "<br><br><font color=white face=verdana size=2><b>Parking lots (ver 14.10 only)</b></font><br><table border=0><td height=45 width=240>&nbsp;<font color=white face=verdana size=2></td></table>";
print "</table>";
 // calculate number of pages needing links
        $pages=intval($numrows/$limit);

        // $pages now contains int of pages needed unless there is a remainder from division
        if ($numrows%$limit) {
        // has remainder so add one page
        $pages++;
        }

        echo "<font size=1>";
        // next we need to do the links to other results
        if (pages!=1)
        {
                if ($pgnum==1) {
                        print "<a href=\"$PHP_SELF?offset=0&pgnum=1\">PREV</a> &nbsp;\n";
                }
                else
                {
                        $prevoffset=$offset-45;
                        $cpgnum = intval($prevoffset/$limit)+1;
                        print "<a href=\"$PHP_SELF?offset=$prevoffset&pgnum=$cpgnum\">PREV</a>&nbsp; \n";
                }
        }

        for ($i=1;$i<=$pages;$i++) { // loop thru
                $newoffset=$limit*($i-1);
                $cpgnum = $i;
                print "<a href=\"$PHP_SELF?offset=$newoffset&pgnum=$cpgnum\">$cpgnum</a>&nbsp; \n";
        }

        // check to see if last page
        if ($pages!=1)
        {
                if ($pgnum<$pages) {
                        $newoffset=$offset+$limit;
                        $cpgnum = intval(($offset+$limit)/$limit)+1;
                        print "<a href=\"$PHP_SELF?offset=$newoffset&pgnum=$cpgnum\">NEXT</a><p>\n";

                }
                else
                {
                        print "<a href=\"$PHP_SELF?offset=$newoffset&pgnum=$pages\">NEXT</a><p>\n";

                }
        }

        // Close database.
        pg_close($conn);

?>
