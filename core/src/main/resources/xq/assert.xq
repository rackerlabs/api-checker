(:
 :   Copyright 2017 Rackspace US, Inc.
 :
 :   Licensed under the Apache License, Version 2.0 (the "License");
 :   you may not use this file except in compliance with the License.
 :   You may obtain a copy of the License at
 :
 :       http://www.apache.org/licenses/LICENSE-2.0
 :
 :   Unless required by applicable law or agreed to in writing, software
 :   distributed under the License is distributed on an "AS IS" BASIS,
 :   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 :   See the License for the specific language governing permissions and
 :   limitations under the License.
 :)
xquery version "3.1" encoding "UTF-8";

module namespace req = "http://www.rackspace.com/repose/wadl/checker/request";

declare namespace map = "http://www.w3.org/2005/xpath-functions/map";
declare namespace xs = "http://www.w3.org/2001/XMLSchema";

declare %private variable $req:__JSON__    as map(*)? external;
declare %private variable $req:__REQUEST__ as map(*) external;
declare %private variable $req:__CONTEXT__ as map(*) external;

declare variable $req:_           := if (not(empty($req:__JSON__))) then $req:__JSON__ else
                                     if (not(empty(/element()))) then (/) else ();
declare variable $req:body        := $req:_;
declare variable $req:method      := $req:__REQUEST__?method;
declare variable $req:uri         := $req:__REQUEST__?uri;
declare variable $req:headerNames := distinct-values((map:keys($req:__REQUEST__?headers), map:keys($req:__CONTEXT__?headers)));
declare variable $req:uriLevel    := $req:__CONTEXT__?uriLevel;


declare function req:headers ($name as xs:string, $split as xs:boolean) as xs:string* {
  if ($split) then
    for $h in req:headers($name) return
    for $s in tokenize($h,',') return normalize-space($s)
  else
    req:headers($name)
};

declare function req:headers ($name as xs:string) as xs:string* {
  let $hn := lower-case($name)
  return ($req:__CONTEXT__?headers($hn), $req:__REQUEST__?headers($hn))
};

declare function req:header ($name as xs:string, $split as xs:boolean) as xs:string? {
  req:headers($name, $split)[1]
};

declare function req:header ($name as xs:string) as xs:string? {
  req:headers($name)[1]
};
