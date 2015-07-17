# SPELL release 2.3.12 - information

# About this release #

SES is proud to announce that **SPELL 2.3.12** is available for download. This version of SPELL includes a number of enhancements and fixes that SES is using in **Orbital Star2**, **Lockheed Martin A2100**, **Astrium E3000**, **TAS SB4000** and **SS/Loral FS1300** satellites.

SPELL is used for operations for all these platforms, using both GMV hifly and SES Scorpio ground control systems.

<br>
<h1>Highlights</h1>

<h2>What is new with respect to 2.0.X</h2>

<ul><li>Support for operations with Astrium Eurostar 3000 in conjunction with automatically converted procedures (with PIL2SPELL software)</li></ul>

<ul><li>Friendly merging, viewing and edition of external data files used by procedures, including support for Astrium data file format.</li></ul>

<ul><li>Usage of <b>data containers</b> that improve execution safety by checking variable initialization, value types, value ranges and expected values.</li></ul>

<ul><li>Improved <b>Monitoring Mode</b> to allow multiple users to follow the execution of a procedure without operational impact.</li></ul>

<ul><li>Migration of core server processes to C++ to improve maintainability and performance</li></ul>

<ul><li><b>Recovery</b> of procedure executions after Python errors based on the contextual information of the execution</li></ul>

<ul><li>Some structural changes in GUI to improve usability.</li></ul>

<h2>Compatibility</h2>

SPELL 2.3.12 is fully backwards-compatible with former SPELL releases. <i>What runs in SPELL 2.0 or 1.5, runs in SPELL 2.3</i>.<br>
<br>
Nevertheless, some new constructs and features have been added to the language specification, therefore <i>what runs in SPELL 2.3</i>may<i>not run in SPELL 2.0 or 1.5</i>.<br>
<br>
<h2>SPELL-DEV</h2>

This release does not include changes to the SPELL-DEV application.