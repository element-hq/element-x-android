/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { screenshots } from './data.js';

const URL_PARAM_LANGUAGES = "l";
const URL_PARAM_IMAGE_WIDTH = "w";
const URL_PARAM_ALL_SCREENSHOTS = "a";
const URL_PARAM_IF_MODIFIED_AFTER = "d";

// Get the base url of the current page
const baseUrl = window.location.href.split('/').slice(0, -1).join('/');
// On localhost, get the English screenshots from the location `../tests/uitests/src/test/snapshots/images`
const isLocalHost = window.location.hostname === "localhost"
let englishBasePath
if (isLocalHost) {
  englishBasePath = `../tests/uitests/src/test/snapshots/images`
} else {
  englishBasePath = `en`
}

const dataLanguages = screenshots[0];

// Read the URL params
const urlParams = new URLSearchParams(window.location.search);

// Get the wanted languages from the url params, or default to "de" and "fr", and ensure "en" is always there
const wantedLanguages = (urlParams.get(URL_PARAM_LANGUAGES) ? urlParams.get(URL_PARAM_LANGUAGES).split(',') : ['de', 'fr']) + ["en"];
// Map dataLanguages to visibleLanguages, set to 1 if the language is in wantedLanguages, 0 otherwise
let visibleLanguages = dataLanguages.map((language) => wantedLanguages.includes(language) ? 1 : 0);
// Read width from the url params, and ensure it's a multiple of 25 and is between 75 and 500
const DEFAULT_WIDTH = 300;
const MIN_WIDTH = 75;
const MAX_WIDTH = 500;
const WIDTH_STEP = 25;
let imageWidth = DEFAULT_WIDTH
let width = urlParams.get(URL_PARAM_IMAGE_WIDTH);
if (width) {
    // Ensure width is an integer, if not use the default value
    width = parseInt(width) || DEFAULT_WIDTH;
    imageWidth = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, Math.round(width / WIDTH_STEP) * WIDTH_STEP));
}
// Read showAllScreenshots from the url params
let showAllScreenshots = urlParams.get(URL_PARAM_ALL_SCREENSHOTS) === 1;
// Read the minimum date of modification from the url params
let minModifiedDayTime = urlParams.get(URL_PARAM_IF_MODIFIED_AFTER);

function updatePageUrl() {
    // Update the URL displayed in the browser without loading again the page
    var queryParams = new URLSearchParams(window.location.search);
    // Add the languages to the url params, without "en"
    const lg = dataLanguages.filter((language, i) => visibleLanguages[i] == 1).filter(l => l != "en").join(',');
    if (lg) {
        queryParams.set(URL_PARAM_LANGUAGES, lg);
    } else {
        queryParams.delete(URL_PARAM_LANGUAGES);
    }
    if (imageWidth != DEFAULT_WIDTH) {
        queryParams.set(URL_PARAM_IMAGE_WIDTH, imageWidth);
    } else {
        queryParams.delete(URL_PARAM_IMAGE_WIDTH);
    }
    if (showAllScreenshots) {
        queryParams.set(URL_PARAM_ALL_SCREENSHOTS, showAllScreenshots ? 1 : 0);
    } else {
        queryParams.delete(URL_PARAM_ALL_SCREENSHOTS);
    }
    if (minModifiedDayTime > 0) {
        queryParams.set(URL_PARAM_IF_MODIFIED_AFTER, minModifiedDayTime);
    } else {
        queryParams.delete(URL_PARAM_IF_MODIFIED_AFTER);
    }
    // Replace the current URL with the new one, including the fragment
    history.replaceState(null, '', `${window.location.pathname}?${queryParams}${window.location.hash}`);
}

function addForm() {
  // Insert the form into the div with id form_container
  const form = document.createElement('form');
  const languageLabel = document.createElement('label');
  languageLabel.textContent = 'Languages:';
  form.appendChild(languageLabel);
  // Add a check box per entry in the dataLanguages
  for (let i = 0; i < dataLanguages.length; i++){
    const label = document.createElement('label');
    const text = document.createTextNode(dataLanguages[i]);
    const input = document.createElement('input');
    input.type = 'checkbox';
    input.disabled = i == 0;
    input.name = dataLanguages[i];
    input.checked = visibleLanguages[i] == 1;
    input.onchange = (e) => {
      if (e.target.checked) {
        visibleLanguages[i] = 1;
      } else {
        visibleLanguages[i] = 0;
      }
      updatePageUrl()
      addTable();
    };
    label.appendChild(input);
    label.appendChild(text);
    form.appendChild(label);
  }
  // Add a break line
  form.appendChild(document.createElement('br'));
  // Add a label with the text "Width"
  const label = document.createElement('label');
  label.textContent = 'Screenshots width:';
  form.appendChild(label);
  // Add a input text to input the width of the image
  const widthInput = document.createElement('input');
  widthInput.id = 'width_input';
  widthInput.type = 'number';
  widthInput.min = MIN_WIDTH;
  widthInput.max = MAX_WIDTH;
  widthInput.step = WIDTH_STEP;
  widthInput.value = imageWidth;
  widthInput.onchange = (e) => {
      imageWidth = e.target.value;
      updatePageUrl();
      addTable();
  };
  form.appendChild(widthInput);
  // Add a label with the text "Show all screenshots"
  const label2 = document.createElement('label');
  label2.textContent = 'Show all screenshots:';
  label2.title = 'Show all screenshots, including those with no translated versions.';
  const input2 = document.createElement('input');
  input2.type = 'checkbox';
  input2.name = "test";
  input2.checked = showAllScreenshots;
  input2.onchange = (e) => {
    showAllScreenshots = e.target.checked;
    updatePageUrl()
    addTable();
  };
  label2.appendChild(input2);
  form.appendChild(label2);
  /*
  // Add a date picker to input the minimum date of modification
  const label3 = document.createElement('label');
  label3.textContent = 'If modified since:';
  form.appendChild(label3);
  const dateInput = document.createElement('input');
  dateInput.type = 'date';
  if (minModifiedDayTime > 0) {
    dateInput.value = new Date(minModifiedDayTime * 86400000).toISOString().split('T')[0];
  }
  dateInput.onchange = (e) => {
      if (e.target.value === "") {
          minModifiedDayTime = 0;
      } else {
          minModifiedDayTime = new Date(e.target.value).getTime() / 86400000;
      }
      updatePageUrl();
      addTable();
  };
  form.appendChild(dateInput);
  */
  // Add a span with id result to display the number of lines
  const lines = document.createElement('span');
  lines.id = 'lines';
  lines.textContent = "...";
  form.appendChild(lines);
  document.getElementById('form_container').appendChild(form);
}

function getNiceName(name) {
  var indices = [];
  for(var i = 0; i < name.length; i++) {
      if (name[i] === "_") indices.push(i);
  }
  return name.substring(indices[0] + 1, indices[1]);
}

function createMissingImageElement() {
    const text = document.createElement('p');
    text.className = "missing";
    text.textContent = 'No image';
    return text;
}

function createUpToDateImageElement() {
    const text = document.createElement('p');
    text.className = "missing";
    text.textContent = 'Image not updated';
    return text;
}

function convertToHumanReadableDate(modifiedDayTime) {
    var date = new Date(modifiedDayTime * 86400000);
    return date.toLocaleDateString();
}

function createImageElement(fullFile, modifiedDayTime) {
    const img = document.createElement('img');
    img.className = "screenshot";
    img.src = `${baseUrl}/${fullFile}`;
    if(modifiedDayTime > 0) {
        img.title = fullFile + "\n\nModified on " + convertToHumanReadableDate(modifiedDayTime);
    } else {
        img.title = fullFile;
    }
    img.alt = "Missing image";
    img.width = imageWidth;
    return img;
}

function addTable() {
  var linesCounter = 0;
  // Remove any previous table
  document.getElementById('screenshots_container').innerHTML = '';
  // screenshots contains a table of screenshots, lets convert to an html table
  const table = document.createElement('table');
  const thead = document.createElement('thead');
  const tbody = document.createElement('tbody');

  // First item of screenshots contains the languages
  // Build the languages row
  const languagesHeaderRow = document.createElement('tr');
  for (let languageIndex = 0; languageIndex < dataLanguages.length; languageIndex++) {
    // Do not add the language if it is hidden
    if (visibleLanguages[languageIndex] == 0) {
        continue;
    }
    const th = document.createElement('th');
    th.textContent = dataLanguages[languageIndex];
    languagesHeaderRow.appendChild(th);
  }
  const numVisibleLanguages = languagesHeaderRow.childElementCount
  // Next items are the data
  var currentHeaderValue = "";
  var screenshotCounter = 0;
  for (let screenshotIndex = 1; screenshotIndex < screenshots.length; screenshotIndex++) {
    let englishFile = screenshots[screenshotIndex][0];
    let niceName = getNiceName(englishFile);
    if (niceName != currentHeaderValue) {
        screenshotCounter = 0;
    }
    const tr = document.createElement('tr');
    tr.id = niceName + screenshotCounter;
    let hasTranslatedFiles = false;
    for (let languageIndex = 0; languageIndex < dataLanguages.length; languageIndex++) {
      if (visibleLanguages[languageIndex] == 0) {
        continue;
      }
      const td = document.createElement('td');
      if (languageIndex == 0) {
        // English file
        td.appendChild(createImageElement(`${englishBasePath}/${englishFile}.png`, 0));
      } else if (languageIndex == 1) {
        // Dark English file
        if (screenshots[screenshotIndex][1].length > 0) {
          hasTranslatedFiles = true;
          td.appendChild(createImageElement(`${englishBasePath}/${screenshots[screenshotIndex][1]}.png`, 0));
        } else {
          td.appendChild(createMissingImageElement());
        }
      } else {
        let modifiedDayTime = screenshots[screenshotIndex][languageIndex];
        if (modifiedDayTime === 0) {
          td.appendChild(createMissingImageElement());
        } else if(modifiedDayTime >= minModifiedDayTime) {
          hasTranslatedFiles = true;
          // Foreign file is the same as the english file, replacing the language
          const foreignFile = englishFile.substring(0, englishFile.length-2) + dataLanguages[languageIndex];
          const fullForeignFile = `${dataLanguages[languageIndex]}/${foreignFile}.png`;
          td.appendChild(createImageElement(fullForeignFile, modifiedDayTime));
        } else {
          td.appendChild(createUpToDateImageElement());
        }
      }
      tr.appendChild(td);
    }
    if (showAllScreenshots || hasTranslatedFiles) {
      linesCounter++;
      // Add a header for row, if different from previous
      if (niceName != currentHeaderValue) {
        currentHeaderValue = niceName;
        const trHead = document.createElement('tr');
        trHead.id = niceName;
        const tdHead = document.createElement('td');
        tdHead.colSpan = numVisibleLanguages;
        tdHead.className = "view-header";
        tdHead.textContent = niceName;
        trHead.appendChild(tdHead);
        tbody.appendChild(trHead);
        tbody.appendChild(languagesHeaderRow.cloneNode(true));
      }
      tbody.appendChild(tr);
    }
    screenshotCounter++;
  }
  table.appendChild(thead);
  table.appendChild(tbody);

  // Add the table to the div with id screenshots_container
  document.getElementById('screenshots_container').appendChild(table);
  // Update the number of lines
  document.getElementById('lines').textContent = `${linesCounter} lines`;
}

addForm();
addTable();
