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

// Read default visible languages from the fragment
const fragment = new URLSearchParams(window.location.hash.substring(1));
// Get the wanted languages from the fragment, or default to "de" and "fr", and ensure "en" is always there
const wantedLanguages = (fragment.get('languages') ? fragment.get('languages').split(',') : ['de', 'fr']) + ["en"];

// Map dataLanguages to visibleLanguages, set to 1 if the language is in wantedLanguages, 0 otherwise
let visibleLanguages = dataLanguages.map((language) => wantedLanguages.includes(language) ? 1 : 0);

let imageWidth = 300;
let showAllScreenshots = false;

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
  widthInput.min = 75;
  widthInput.max = 500;
  widthInput.step = 25;
  widthInput.value = imageWidth;
  widthInput.onchange = (e) => {
      imageWidth = e.target.value;
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
    addTable();
  };
  label2.appendChild(input2);
  form.appendChild(label2);
  document.getElementById('form_container').appendChild(form);
}

function getNiceName(name) {
  var indices = [];
  for(var i = 0; i < name.length; i++) {
      if (name[i] === "_") indices.push(i);
  }
  return name.substring(indices[2] + 1, indices[3]);
}

function addTable() {
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
  for (let screenshotIndex = 1; screenshotIndex < screenshots.length; screenshotIndex++) {
    let englishFile = screenshots[screenshotIndex][0];
    const tr = document.createElement('tr');
    let hasTranslatedFiles = false;
    for (let languageIndex = 0; languageIndex < dataLanguages.length; languageIndex++) {
      if (visibleLanguages[languageIndex] == 0) {
        continue;
      }
      const td = document.createElement('td');
      if (languageIndex == 0) {
        const fullFile = `${englishBasePath}/${englishFile}.png`;
        const img = document.createElement('img');
        img.className = "screenshot";
        img.src = `${baseUrl}/${fullFile}`;
        img.title = fullFile;
        img.alt = "Missing image";
        img.width = imageWidth;
        td.appendChild(img);
      } else {
        let hasFile = screenshots[screenshotIndex][languageIndex];
        if (hasFile === 0) {
          const text = document.createElement('p');
          text.className = "missing";
          text.textContent = 'No image';
          td.appendChild(text);
        } else {
          hasTranslatedFiles = true;
          // Foreign file is the same as the english file, replacing the language
          const foreignFile = englishFile.replace("en]", `${dataLanguages[languageIndex]}]`).replace("_S_", "_T_")
          const fullForeignFile = `${dataLanguages[languageIndex]}/${foreignFile}.png`;
          const img = document.createElement('img');
          img.className = "screenshot";
          img.src = `${baseUrl}/${fullForeignFile}`;
          img.title = fullForeignFile;
          img.alt = "Missing image";
          img.width = imageWidth;
          td.appendChild(img);
        }
      }
      tr.appendChild(td);
    }
    if (showAllScreenshots || hasTranslatedFiles) {
      // Add a header for row, if different from previous
      let name = getNiceName(englishFile);
      if (name != currentHeaderValue) {
        currentHeaderValue = name;
        const trHead = document.createElement('tr');
        const tdHead = document.createElement('td');
        tdHead.colSpan = numVisibleLanguages;
        tdHead.className = "view-header";
        tdHead.textContent = name;
        trHead.appendChild(tdHead);
        tbody.appendChild(trHead);
        tbody.appendChild(languagesHeaderRow.cloneNode(true));
      }
      tbody.appendChild(tr);
    }
  }
  table.appendChild(thead);
  table.appendChild(tbody);

  // Add the table to the div with id screenshots_container
  document.getElementById('screenshots_container').appendChild(table);
}

addForm();
addTable();
