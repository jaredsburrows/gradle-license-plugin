# Change Log

## Version 0.9.8 *(2024-06-06)*
 * [#496](https://github.com/jaredsburrows/gradle-license-plugin/pull/496) Make sure dependencies are sorted deterministically
 * [#465](https://github.com/jaredsburrows/gradle-license-plugin/pull/465) Don't show versions in html
 * [#464](https://github.com/jaredsburrows/gradle-license-plugin/pull/464) Add dark mode support for html
 * [#463](https://github.com/jaredsburrows/gradle-license-plugin/pull/463) POM artifact resolution
 * [#439](https://github.com/jaredsburrows/gradle-license-plugin/pull/439) Remove debug print
 * [#433](https://github.com/jaredsburrows/gradle-license-plugin/pull/433) Sort project group order in HTML report using license keys
 * [#431](https://github.com/jaredsburrows/gradle-license-plugin/pull/431) Sort set of licenses in HTML report

Many thanks to
[@monae](https://github.com/monae)
[@t-beckmann](https://github.com/t-beckmann)
[@francescocervone](https://github.com/francescocervone)
for the code contributions!

## Version 0.9.7 *(2024-01-30)*
 * [#422](https://github.com/jaredsburrows/gradle-license-plugin/pull/422) Bump compileOnly AGP to 7.2.2

## Version 0.9.6 *(2024-01-30)*
 * [#418](https://github.com/jaredsburrows/gradle-license-plugin/pull/418) Apply Ktlint 12.10
 * [#417](https://github.com/jaredsburrows/gradle-license-plugin/pull/417) Bump compileOnly AGP to 7.1.3

## Version 0.9.5 *(2024-01-30)*
 * [#416](https://github.com/jaredsburrows/gradle-license-plugin/pull/416) Bump compileOnly AGP to 7.0.4
 * [#415](https://github.com/jaredsburrows/gradle-license-plugin/pull/415) Bump compileOnly AGP to 4.2.2
 * [#413](https://github.com/jaredsburrows/gradle-license-plugin/pull/413) Bump compileOnly AGP to 4.0.2
 * [#411](https://github.com/jaredsburrows/gradle-license-plugin/pull/411) Update dialog code to remove warning
 * [#407](https://github.com/jaredsburrows/gradle-license-plugin/pull/407) Run tests against Android 34
 * [#406](https://github.com/jaredsburrows/gradle-license-plugin/pull/406) Make sure HTML reports are valid
 * [#402](https://github.com/jaredsburrows/gradle-license-plugin/pull/402) Add keys for list of known licenses

Many thanks to
[@monae](https://github.com/monae)
for the code contributions!

## Version 0.9.4 *(2024-01-25)*
 * [#398](https://github.com/jaredsburrows/gradle-license-plugin/pull/398) Fix exponential execution time for resolving complex dependencies
 * [#312](https://github.com/jaredsburrows/gradle-license-plugin/pull/312) Quiet "Cannot resolve configuration" warnings

Many thanks to
[@realdadfish](https://github.com/realdadfish)
[@monae](https://github.com/monae)
for the code contributions!

## Version 0.9.3 *(2023-06-20)*
 * [#291](https://github.com/jaredsburrows/gradle-license-plugin/pull/291) Use ReaderFactory.newXmlReader to fix #275

## Version 0.9.2 *(2023-03-20)*
 * [#247](https://github.com/jaredsburrows/gradle-license-plugin/pull/247) CsvReport: missing values converted to empty strings
 * [#245](https://github.com/jaredsburrows/gradle-license-plugin/pull/245) CsvReport: escape special characters
 * [#242](https://github.com/jaredsburrows/gradle-license-plugin/pull/242) Support plain-Kotlin Gradle projects
 * [#218](https://github.com/jaredsburrows/gradle-license-plugin/pull/218) Replaced ignoredGroups with ignoredPatterns
 * [#213](https://github.com/jaredsburrows/gradle-license-plugin/pull/213) Re-run the license task on the project configurations/dependencies changes
 * [#205](https://github.com/jaredsburrows/gradle-license-plugin/pull/205) Use moshi
 * [#202](https://github.com/jaredsburrows/gradle-license-plugin/pull/202) Use maven parser
 * [#168](https://github.com/jaredsburrows/gradle-license-plugin/pull/168) Added option to ignore a set of group IDs from the report
 * [#159](https://github.com/jaredsburrows/gradle-license-plugin/pull/159) Update html css to support word-breaking on long lines
 * [#58](https://github.com/jaredsburrows/gradle-license-plugin/pull/58) Feature - option to copy reports into variant-specific asset directories

Many thanks to
[@danielesegato](https://github.com/danielesegato)
[@mudkiplex](https://github.com/mudkiplex)
[@rabidaudio](https://github.com/rabidaudio)
[@nisrulz](https://github.com/nisrulz)
for the code contributions!

## Version 0.9.0 *(2022-04-22)*
 * Use Maven APIs
 * [#191](https://github.com/jaredsburrows/gradle-license-plugin/pull/191) Fix license report task in multi-project setups
 * [#190](https://github.com/jaredsburrows/gradle-license-plugin/pull/190) Fix copyCsvReportToAssets in README.md
 * [#184](https://github.com/jaredsburrows/gradle-license-plugin/pull/184) Plugin cannot be used in non-Android application projects

Many thanks to
[@flobetz](https://github.com/fllink)
[@Bradan](https://github.com/Bradan)
for the code contributions!

## Version 0.8.91 *(2022-04-10)*
 * [#172](https://github.com/jaredsburrows/gradle-license-plugin/pull/172) Avoid processing *.aar dependencies which can not be parsed and cause gradle failures

Many thanks to
[@flobetz](https://github.com/flobetz)
for the code contributions!

## Version 0.8.90 *(2020-12-30)*
 * [#140](https://github.com/jaredsburrows/gradle-license-plugin/pull/140) Added CSV report support
 * [#134](https://github.com/jaredsburrows/gradle-license-plugin/pull/134) Add android library subproject dependencies to the report

Many thanks to
[@mkubiczek](https://github.com/mkubiczek)
for the code contributions!

## Version 0.8.80 *(2020-05-29)*
 * [#127](https://github.com/jaredsburrows/gradle-license-plugin/pull/127) Remove instant app plugin, support feature, library and application

## Version 0.8.70 *(2020-03-03)*
 * Fix bad versioning - 0.8.7 -> 0.8.70, new snapshot is 0.8.80-SNAPSHOT

Many thanks to
[@PaulWoitaschek](https://github.com/PaulWoitaschek)
for the code contributions!

## Version 0.8.7 *(2020-03-01)*
 * [#112](https://github.com/jaredsburrows/gradle-license-plugin/pull/112) Fix windows file path
 * [#114](https://github.com/jaredsburrows/gradle-license-plugin/pull/114) Include Copyright owner/date in HTML license report; show multiple licenses
 * [#118](https://github.com/jaredsburrows/gradle-license-plugin/pull/118) Add another official spelling of the MIT License URL

Many thanks to
[@DonnKey](https://github.com/DonnKey)
for the code contributions!

## Version 0.8.6 *(2019-10-14)*
 * Finished converting plugin to Kotlin
 * [#99](https://github.com/jaredsburrows/gradle-license-plugin/pull/99) Make sure to use correct Path.Separator
 * [#102](https://github.com/jaredsburrows/gradle-license-plugin/pull/102) Initiate extension earlier in setup process
 * [#103](https://github.com/jaredsburrows/gradle-license-plugin/pull/103) Use Console Renderer to handle cross platform terminal printing

## Version 0.8.5 *(2019-04-23)*
 * Converted many files to Kotlin
 * [#57](https://github.com/jaredsburrows/gradle-license-plugin/pull/57) Eliminate duplicate licenses
 * [#76](https://github.com/jaredsburrows/gradle-license-plugin/pull/76) Use double quotes for HTML
 * [#69](https://github.com/jaredsburrows/gradle-license-plugin/pull/69) Undesired capitalisation
 * [#55](https://github.com/jaredsburrows/gradle-license-plugin/pull/55) Running multiple build variant license tasks crashes build
 * [#48](https://github.com/jaredsburrows/gradle-license-plugin/pull/48) Version is null for some dependencies

Many thanks to
[@iankerr](https://github.com/iankerr)
for the code contributions!

## Version 0.8.42 *(2018-11-21)*
 * [#44](https://github.com/jaredsburrows/gradle-license-plugin/pull/44) Extra report info
 * [#46](https://github.com/jaredsburrows/gradle-license-plugin/pull/46) Android tests fail on Windows
 * [#53](https://github.com/jaredsburrows/gradle-license-plugin/pull/53) Make any links to licenses clickable in preformatted text 
 * Add Apache 2 to the license map, Common for Square libraries
 
Many thanks to
[@markhoughton](https://github.com/markhoughton),
[@iankerr](https://github.com/iankerr)
for the code contributions!

## Version 0.8.41 *(2018-03-14)*
## Version 0.8.3 *(2018-03-14)*
## Version 0.8.2 *(2018-03-14)*
 * [#24](https://github.com/jaredsburrows/gradle-license-plugin/issues/24) Show complete license text

## Version 0.8.1 *(2018-01-24)*
## Version 0.8.0 *(2018-01-22)*
 * [#27](https://github.com/jaredsburrows/gradle-license-plugin/pull/27) Recursively scan parent POMs for license
 * [#28](https://github.com/jaredsburrows/gradle-license-plugin/pull/29) Support multiple licenses; add more attributes to json
 * [#30](https://github.com/jaredsburrows/gradle-license-plugin/pull/30) Added configuration options

Many thanks to
[@ChristianCiach](https://github.com/ChristianCiach),
[@MatthewDavidBradshaw](https://github.com/MatthewDavidBradshaw)
for the code contributions!

## Version 0.7.0 *(2017-10-21)*
 * [#15](https://github.com/jaredsburrows/gradle-license-plugin/issues/15) Square Libs missing
 * Add support for 'api' and 'implementation'
 * Added logging of missing names, licenses and validating urls for POM files

## Version 0.6.0 *(2017-06-03)*

## Version 0.5.0 *(2017-05-08)*
 * Add groovy doc

## Version 0.4.0 *(2017-01-09)*
 * Add developers name to reports
 * Remove added repositories through the plugin
 * Add HTML support

## Version 0.2.0 *(2016-12-28)*
 * Add JSON report support
 * Add Java projects support
 * [#6](https://github.com/jaredsburrows/gradle-spoon-plugin/pull/6) Multiple reruns causes appending HTML

## Version 0.1.0 *(2016-12-21)*
 * Initial release
