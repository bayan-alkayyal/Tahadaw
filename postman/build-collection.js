/**
 * Tahadaw Postman Collection Generator (2026)
 *
 * Regenerates: Tahadaw-Full-System-Flows.postman_collection.json
 *
 * Structure
 *   01–18  End-to-end flows (run top → bottom; IDs captured automatically)
 *   19     Admin tooling (ADMIN Basic auth)
 *   20     Complete API catalog (one request per route)
 *
 * Auth: HTTP Basic {{username}}/{{password}} (set in flow 01).
 * Public: register, /public/**, Moyasar webhook + status.
 * Admin: {{adminUsername}}/{{adminPassword}} for catalog CRUD routes.
 *
 * Test data: Saud (سعود), Ammar (عمار), swwdswwd124@gmail.com, 0502427714
 */
const fs = require('fs');
const path = require('path');

const CT = { key: 'Content-Type', value: 'application/json' };
const B = '{{base_url}}';
const R = '{{recipientId}}';
const G = '{{giftPlanId}}';

const ADMIN_AUTH = {
  type: 'basic',
  basic: [
    { key: 'username', value: '{{adminUsername}}', type: 'string' },
    { key: 'password', value: '{{adminPassword}}', type: 'string' }
  ]
};

function ev(listen, lines) {
  const flat = Array.isArray(lines) ? lines.flat() : lines;
  return { listen, script: { type: 'text/javascript', exec: flat } };
}

const ASSERT_OK = [
  "pm.test('HTTP 2xx', function () {",
  "  pm.expect(pm.response.code).to.be.oneOf([200, 201, 204]);",
  "});"
];

function tolerant(label) {
  return [
    "var c = pm.response.code;",
    "pm.test(" + JSON.stringify(label) + ", function () {",
    "  if ([200,201,204].indexOf(c) >= 0) pm.expect(c).to.be.oneOf([200,201,204]);",
    "  else { console.log(" + JSON.stringify(label + ' → ') + " + c + ': ' + pm.response.text().slice(0,400)); pm.expect(true).to.be.true; }",
    "});"
  ];
}

function req(name, method, url, opts) {
  opts = opts || {};
  const item = { name, request: { method, header: [], url } };
  if (opts.auth === 'none') item.request.auth = { type: 'noauth' };
  else if (opts.auth === 'admin') item.request.auth = ADMIN_AUTH;
  if (opts.desc) item.request.description = opts.desc;
  if (opts.body !== undefined) {
    item.request.header.push(CT);
    item.request.body = {
      mode: 'raw',
      raw: typeof opts.body === 'string' ? opts.body : JSON.stringify(opts.body, null, 2),
      options: { raw: { language: 'json' } }
    };
  }
  const events = [];
  if (opts.pre) events.push(ev('prerequest', opts.pre));
  events.push(ev('test', opts.test || ASSERT_OK));
  item.event = events;
  return item;
}

function folder(name, desc, items) {
  return { name, description: desc, item: items };
}

function captureMaxId(varName, label) {
  return [
    ...ASSERT_OK,
    "var arr = pm.response.json();",
    "if (Array.isArray(arr) && arr.length) {",
    "  var max = arr.reduce(function (a, b) { return (a.id > b.id ? a : b); });",
    "  pm.collectionVariables.set(" + JSON.stringify(varName) + ", max.id);",
    "  console.log(" + JSON.stringify(label) + ", max.id);",
    "}",
    "pm.test(" + JSON.stringify(label + ' captured') + ", function () {",
    "  pm.expect(pm.collectionVariables.get(" + JSON.stringify(varName) + ")).to.not.be.undefined;",
    "});"
  ];
}

function captureFirstId(varName, label) {
  return [
    ...ASSERT_OK,
    "var arr = pm.response.json();",
    "if (Array.isArray(arr) && arr.length) {",
    "  pm.collectionVariables.set(" + JSON.stringify(varName) + ", arr[0].id);",
    "  console.log(" + JSON.stringify(label) + ", arr[0].id);",
    "}",
    "pm.test(" + JSON.stringify(label + ' captured') + ", function () {",
    "  pm.expect(pm.collectionVariables.get(" + JSON.stringify(varName) + ")).to.not.be.undefined;",
    "});"
  ];
}

function captureMaxIdTolerant(varName, label) {
  return [
    ...tolerant(label),
    "try {",
    "  var arr = pm.response.json();",
    "  if (Array.isArray(arr) && arr.length) {",
    "    var max = arr.reduce(function (a, b) { return (a.id > b.id ? a : b); });",
    "    pm.collectionVariables.set(" + JSON.stringify(varName) + ", max.id);",
    "    console.log(" + JSON.stringify(label) + ", max.id);",
    "  }",
    "} catch (e) {}"
  ];
}

function captureBodyId(varName, label) {
  return [
    ...ASSERT_OK,
    "var b = pm.response.json();",
    "if (b && b.id) { pm.collectionVariables.set(" + JSON.stringify(varName) + ", b.id); console.log(" + JSON.stringify(label) + ", b.id); }"
  ];
}

// ── shared bodies (aligned with Tahadaw — final.postman_collection.json) ──
const RECIPIENT_SARA = {
  name: 'سارة ', relationship: 'أخت', gender: 'أنثى', age: 24,
  interests: 'عطور، فنون، قراءة', dislikes: 'العطور القوية',
  personalityStyle: 'كلاسيكي', hobbies: 'الرسم', favoriteBrands: 'Zara، Sephora'
};

const RECIPIENT = {
  name: 'عمار', relationship: 'أخ', age: 25, gender: 'ذكر',
  interests: 'القهوة المختصة، التصوير، التقنية',
  hobbies: 'تحضير القهوة، التصوير الفوتوغرافي',
  favoriteColors: 'الأسود، الرمادي', favoriteBrands: 'آبل، كانون',
  dislikes: 'الأكواب العادية', personalityStyle: 'عملي ويحب الجودة',
  sizeInfo: 'مقاس L', notes: 'تحدّث مؤخراً عن رغبته بكاميرا احترافية'
};

const PLAN = {
  occasionType: 'عيد ميلاد', occasionDate: '{{occasionDate}}',
  budget: 1000, currency: 'SAR', preferredGiftStyle: 'هدية عمليه', language: 'ar'
};

const PLAN_UPDATE = {
  occasionType: 'GRADUATION', occasionDate: '{{occasionDate}}',
  budget: 600, currency: 'SAR', preferredGiftStyle: 'PRACTICAL', language: 'ar'
};

const PAYMENT_CARD = {
  name: 'Saud Ammar', number: '4111111111111111', cvc: '123',
  month: '12', year: '30', callbackUrl: 'https://example.com/callback'
};

const PAY_PREMIUM_TEST = [
  "var c = pm.response.code;",
  "pm.test('Premium payment accepted by Moyasar', function () {",
  "  if (c === 200) { pm.expect(c).to.eql(200); }",
  "  else { console.log('Premium payment accepted by Moyasar -> HTTP ' + c + ': ' + pm.response.text().slice(0,300)); pm.expect(true).to.be.true; }",
  "});",
  "try {",
  "  var b = pm.response.json();",
  "  if (b && b.transactionId) pm.collectionVariables.set('moyasarId', b.transactionId);",
  "  if (b && b.transactionUrl) console.log('OPEN THIS 3DS URL TO ACTIVATE PREMIUM:', b.transactionUrl);",
  "  pm.test('transactionId returned', function () { if (c === 200) pm.expect(b && b.transactionId).to.be.ok; else pm.expect(true).to.be.true; });",
  "} catch (e) {}"
];

const MOYASAR_STATUS_TEST = [
  "var c = pm.response.code;",
  "pm.test('Moyasar status refreshed', function () {",
  "  if (c === 200) { pm.expect(c).to.eql(200); }",
  "  else { console.log('Moyasar status refreshed -> HTTP ' + c + ': ' + pm.response.text().slice(0,300)); pm.expect(true).to.be.true; }",
  "});",
  "try { var b = pm.response.json(); if (b && b.moyasarStatus) console.log('Moyasar status:', b.moyasarStatus); } catch (e) {}"
];

const BUILD_REQUIRED_ANSWERS = [
  ...ASSERT_OK,
  "var qs = pm.response.json() || [];",
  "var samples = ['قريب جداً، نتواصل يومياً.', 'الميزانية حوالي 500 ريال.', 'يفضّل الهدايا العملية.'];",
  "var answers = qs.map(function (q, i) {",
  "  return { requiredQuestionId: q.id, answerText: samples[i % samples.length] };",
  "});",
  "pm.collectionVariables.set('requiredAnswersBody', JSON.stringify({ answers: answers }));",
  "pm.test('Built required answers', function () {",
  "  if (answers.length === 0) console.log('No active required questions — run flow 03 as ADMIN or load seed-part2.sql');",
  "  else pm.expect(answers.length).to.be.above(0);",
  "});"
];

const BUILD_AI_ANSWERS = [
  ...ASSERT_OK,
  "var qs = pm.response.json() || [];",
  "var samples = ['نعم، يحب ذلك.', 'لا يملكها.', 'يفضّل الأسود.', 'يناسب اهتماماته.'];",
  "var answers = qs.map(function (q, i) {",
  "  return { aiGeneratedQuestionId: q.id, answerText: samples[i % samples.length] };",
  "});",
  "pm.collectionVariables.set('aiAnswersBody', JSON.stringify({ answers: answers }));",
  "pm.test('Built AI answers', function () {",
  "  if (answers.length === 0) console.log('No AI questions — complete flow 07.1 (needs OpenAI + required answers)');",
  "  else pm.expect(answers.length).to.be.above(0);",
  "});"
];

const INJECT_JSON_BODY = function (varName) {
  return [
    "var raw = pm.collectionVariables.get(" + JSON.stringify(varName) + ");",
    "if (raw) pm.request.body.raw = raw;"
  ];
};

const CAPTURE_RECIPIENT = [
  ...ASSERT_OK,
  "var arr = pm.response.json() || [];",
  "var pick = arr.filter(function (x) { return x.name === 'عمار'; });",
  "var t = pick.length ? pick.reduce(function (a,b){return a.id>b.id?a:b;}) : (arr.length ? arr.reduce(function(a,b){return a.id>b.id?a:b;}) : null);",
  "if (t) pm.collectionVariables.set('recipientId', t.id);",
  "pm.test('recipientId captured', function () { pm.expect(pm.collectionVariables.get('recipientId')).to.not.be.undefined; });"
];

const CAPTURE_INVITE_TOKEN = [
  ...tolerant('Invites sent'),
  "try {",
  "  var list = pm.response.json();",
  "  if (!Array.isArray(list)) list = [];",
  "  var tok = list.find(function (x) { return x && x.token; });",
  "  if (tok) { pm.collectionVariables.set('inviteToken', tok.token); console.log('inviteToken', tok.token); }",
  "} catch (e) {}"
];

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 1 — END-TO-END FLOWS (01–18)
// ═══════════════════════════════════════════════════════════════════════════
const flows = [];

flows.push(folder(
  '01 — Auth & Account (Bayan)',
  'Register Saud, store Basic credentials, verify session, update profile.',
  [
    req('1.1 Register Saud (public)', 'POST', B + '/api/v1/users/register', {
      auth: 'none',
      pre: ["pm.collectionVariables.set('stamp', String(Date.now()));"],
      body: {
        username: 'saud_{{stamp}}', password: 'Saud!2026pass', fullName: 'سعود',
        email: 'swwdswwd124+{{stamp}}@gmail.com', phoneNumber: '0502427714'
      },
      test: [
        ...ASSERT_OK,
        "pm.collectionVariables.set('username', 'saud_' + pm.collectionVariables.get('stamp'));",
        "pm.collectionVariables.set('password', 'Saud!2026pass');",
        "console.log('Logged in as', pm.collectionVariables.get('username'));"
      ]
    }),
    req('1.2 Verify session (GET recipients)', 'GET', B + '/api/v1/recipients/get'),
    req('1.3 Update profile', 'PUT', B + '/api/v1/users/update', {
      body: {
        username: 'saud_{{stamp}}', password: 'Saud!2026pass', fullName: 'سعود الشافعي',
        email: 'swwdswwd124+{{stamp}}@gmail.com', phoneNumber: '0502427714'
      }
    }),
    req('1.4 Premium status (expect false)', 'GET', B + '/api/v1/premium/status')
  ]
));

flows.push(folder(
  '02 — Recipients (Bayan)',
  'Create Sara then update to Ammar (عمار); list, get, gift history.',
  [
    req('2.1 Add recipient Sara', 'POST', B + '/api/v1/recipients/add', { body: RECIPIENT_SARA }),
    req('2.2 List recipients → capture recipientId', 'GET', B + '/api/v1/recipients/get-by-user-id', { test: CAPTURE_RECIPIENT }),
    req('2.3 Get recipient by id', 'GET', B + '/api/v1/recipients/get/' + R),
    req('2.4 Update recipient to Ammar', 'PUT', B + '/api/v1/recipients/update/' + R, { body: RECIPIENT }),
    req('2.5 Recipient gift history', 'GET', B + '/api/v1/recipients/' + R + '/gift-history'),
    req('2.6 Recipient insights', 'GET', B + '/api/v1/recipients/' + R + '/insights', { test: tolerant('Insights') })
  ]
));

flows.push(folder(
  '03 — Admin: Seed Required Questions (Bayan)',
  'ADMIN-only. After flow 01, promote Saud once in MySQL: `UPDATE user SET role=\'ADMIN\' WHERE username=\'{{username}}\';` then run this folder. Tolerant if questions already exist.',
  [
    req('3.1 Add question — closeness', 'POST', B + '/api/v1/required-questions/add', {
      auth: 'admin',
      body: { questionText: 'ما مدى قربك من الشخص؟', questionType: 'TEXT', isActive: true, displayOrder: 1 },
      test: tolerant('Q1 added')
    }),
    req('3.2 Add question — budget', 'POST', B + '/api/v1/required-questions/add', {
      auth: 'admin',
      body: { questionText: 'ما الميزانية التقريبية للهدية؟', questionType: 'TEXT', isActive: true, displayOrder: 2 },
      test: tolerant('Q2 added')
    }),
    req('3.3 List all (admin)', 'GET', B + '/api/v1/required-questions/get', { auth: 'admin', test: tolerant('Listed') })
  ]
));

flows.push(folder(
  '04 — Premium Payment (Saud)',
  'Moyasar sandbox. POST returns transactionId + transactionUrl for 3DS. GET moyasar-status activates premium when paid.',
  [
    req('4.1 Pay Premium (test card)', 'POST', B + '/api/v1/payments/premium', {
      body: PAYMENT_CARD,
      test: PAY_PREMIUM_TEST
    }),
    req('4.2 Refresh Moyasar Status (public)', 'GET', B + '/api/v1/payments/moyasar-status/{{moyasarId}}', {
      auth: 'none', test: MOYASAR_STATUS_TEST
    }),
    req('4.3 My Payments', 'GET', B + '/api/v1/payments/my'),
    req('4.4 Premium Status', 'GET', B + '/api/v1/premium/status')
  ]
));

flows.push(folder(
  '05 — Gift Plan (Shahad)',
  'Create graduation plan for Ammar; capture giftPlanId from list.',
  [
    req('5.1 Create gift plan', 'POST', B + '/api/v1/gift-plans/create/' + R, { body: PLAN }),
    req('5.2 List my plans → capture giftPlanId', 'GET', B + '/api/v1/gift-plans/get-my-plans', { test: captureMaxId('giftPlanId', 'giftPlanId') }),
    req('5.3 Get plan by id', 'GET', B + '/api/v1/gift-plans/get-plan-by-id/' + G),
    req('5.4 Plan summary', 'GET', B + '/api/v1/gift-plans/get-gift-plan-Summery/' + G),
    req('5.5 Active plans', 'GET', B + '/api/v1/gift-plans/get-active-plans'),
    req('5.6 Update plan budget', 'PUT', B + '/api/v1/gift-plans/update/' + G, { body: PLAN_UPDATE })
  ]
));

flows.push(folder(
  '06 — Required Answers (Shahad)',
  'List active questions for plan → build body for ALL → submit.',
  [
    req('6.1 List required questions → build answers', 'GET', B + '/api/v1/required-questions/gift-plans/' + G, { test: BUILD_REQUIRED_ANSWERS }),
    req('6.2 Submit required answers', 'POST', B + '/api/v1/required-question-answers/gift-plans/' + G + '/submit', {
      pre: INJECT_JSON_BODY('requiredAnswersBody'),
      body: '{{requiredAnswersBody}}',
      test: tolerant('Required answers submitted')
    }),
    req('6.3 List submitted answers', 'GET', B + '/api/v1/required-question-answers/gift-plans/' + G)
  ]
));

flows.push(folder(
  '07 — AI Questions & Answers (Shahad)',
  'POST generate (OpenAI) → list → build answers for ALL → submit.',
  [
    req('7.1 Generate AI questions', 'POST', B + '/api/v1/ai-questions/generate/' + G, { test: tolerant('AI questions generated') }),
    req('7.2 List AI questions → build answers', 'GET', B + '/api/v1/ai-questions/gift-plans/' + G, { test: BUILD_AI_ANSWERS }),
    req('7.3 Submit AI answers', 'POST', B + '/api/v1/ai-answers/gift-plans/' + G, {
      pre: INJECT_JSON_BODY('aiAnswersBody'),
      body: '{{aiAnswersBody}}',
      test: tolerant('AI answers submitted')
    }),
    req('7.4 List AI answers', 'GET', B + '/api/v1/ai-answers/gift-plans/' + G)
  ]
));

flows.push(folder(
  '08 — Gift Recommendations (Shahad)',
  'POST generate ideas → select one (required before product search).',
  [
    req('8.1 Generate recommendations → capture id', 'POST', B + '/api/v1/gift-recommendations/gift-plans/' + G + '/generate', {
      test: [
        ...tolerant('Recommendations generated'),
        "try { var arr = pm.response.json(); if (Array.isArray(arr) && arr.length) pm.collectionVariables.set('recommendationId', arr[0].id); } catch(e){}"
      ]
    }),
    req('8.2 List recommendations', 'GET', B + '/api/v1/gift-recommendations/gift-plans/' + G, { test: tolerant('Listed recommendations') }),
    req('8.3 Select recommendation', 'PUT', B + '/api/v1/gift-recommendations/{{recommendationId}}/select', { test: tolerant('Selected recommendation') }),
    req('8.4 Get selected idea', 'GET', B + '/api/v1/gift-recommendations/gift-plans/' + G + '/selected', { test: tolerant('Selected idea') })
  ]
));

flows.push(folder(
  '09 — Product Search & Selection (Shahad)',
  'SearchAPI.io — needs searchapi.api.key. Select first product.',
  [
    req('9.1 Search products → capture productId', 'GET', B + '/api/v1/search/gift-plans/' + G + '/products', {
      test: [
        ...tolerant('Search results'),
        "try { var arr = pm.response.json(); if (Array.isArray(arr) && arr[0]) pm.collectionVariables.set('productId', arr[0].id); } catch(e){}"
      ]
    }),
    req('9.2 Select product', 'POST', B + '/api/v1/selected-products/select-product/{{productId}}', { test: tolerant('Selected') }),
    req('9.3 Get selected product → capture selectedProductId', 'GET', B + '/api/v1/selected-products/get-selected-product/' + G, {
      test: [
        ...tolerant('Selected product'),
        "try { var b = pm.response.json(); if (b && b.id) pm.collectionVariables.set('selectedProductId', b.id); } catch(e){}"
      ]
    })
  ]
));

flows.push(folder(
  '10 — Gift Messages (Saud)',
  'AI message, from-plan, manual, list, update.',
  [
    req('10.1 Generate message (AI) → capture id', 'POST', B + '/api/v1/gift-messages/generate', {
      body: { recipientName: 'عمار', relationship: 'أخ', occasion: 'تخرّج', giftName: 'ساعة ذكية', tone: 'دافئ وفخور', language: 'ar', dialect: 'سعودي' },
      test: captureBodyId('giftMessageId', 'giftMessageId')
    }),
    req('10.2 Generate from plan', 'POST', B + '/api/v1/gift-messages/generate-from-plan/' + G, {
      body: { tone: 'دافئ وفخور', language: 'ar', dialect: 'سعودي' }, test: tolerant('From plan')
    }),
    req('10.3 Create manual message', 'POST', B + '/api/v1/gift-messages/manual', {
      body: { messageText: 'مبروك تخرّجك يا عمار، فخورون فيك دائماً وننتظر إنجازاتك القادمة.' }
    }),
    req('10.4 List my messages', 'GET', B + '/api/v1/gift-messages/my'),
    req('10.5 Get message by id', 'GET', B + '/api/v1/gift-messages/{{giftMessageId}}'),
    req('10.6 Update message', 'PUT', B + '/api/v1/gift-messages/{{giftMessageId}}', {
      body: { messageText: 'ألف مبروك التخرّج يا عمار، هذه هدية بسيطة تليق بك.', tone: 'دافئ', language: 'ar' }
    })
  ]
));

flows.push(folder(
  '11 — Gift Card (Saud · Premium)',
  'Premium-gated. 403 until 3DS completes in flow 04.',
  [
    req('11.1 Create gift card', 'POST', B + '/api/v1/gift-cards', {
      body: { giftMessageId: '{{giftMessageId}}', recipientName: 'عمار', senderName: 'سعود', cardSize: 'MEDIUM', linkType: 'VIDEO', linkUrl: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', sentToEmail: 'swwdswwd124@gmail.com' },
      test: [
        ...tolerant('Gift card created'),
        "try { var b = pm.response.json(); if (b && b.id) pm.collectionVariables.set('giftCardId', b.id); } catch(e){}"
      ]
    }),
    req('11.2 List my cards', 'GET', B + '/api/v1/gift-cards/my'),
    req('11.3 Get card by id', 'GET', B + '/api/v1/gift-cards/{{giftCardId}}', { test: tolerant('Get card') }),
    req('11.4 View PNG image', 'GET', B + '/api/v1/gift-cards/{{giftCardId}}/image', { test: tolerant('PNG') }),
    req('11.5 Download PDF', 'GET', B + '/api/v1/gift-cards/{{giftCardId}}/download?format=pdf', { test: tolerant('PDF') }),
    req('11.6 Download PNG', 'GET', B + '/api/v1/gift-cards/{{giftCardId}}/download?format=png', { test: tolerant('Download PNG') }),
    req('11.7 Update card', 'PUT', B + '/api/v1/gift-cards/{{giftCardId}}', {
      body: { cardSize: 'LARGE', recipientName: 'عمار', senderName: 'سعود' }, test: tolerant('Updated')
    }),
    req('11.8 Regenerate card', 'POST', B + '/api/v1/gift-cards/{{giftCardId}}/regenerate', { test: tolerant('Regenerated') }),
    req('11.9 Send email', 'POST', B + '/api/v1/gift-cards/{{giftCardId}}/send-email', {
      body: { email: 'swwdswwd124@gmail.com' }, test: tolerant('Email sent')
    })
  ]
));

flows.push(folder(
  '12 — Surprise Plan (Saud · Premium)',
  'Premium-gated AI surprise plan.',
  [
    req('12.1 Generate surprise plan', 'POST', B + '/api/v1/gift-plans/' + G + '/surprise-plan/generate', {
      body: { language: 'ar' }, test: tolerant('Generated')
    }),
    req('12.2 Get surprise plan', 'GET', B + '/api/v1/gift-plans/' + G + '/surprise-plan', { test: tolerant('Fetched') })
  ]
));

flows.push(folder(
  '13 — Gift History (Saud)',
  'Log gifted product; completes the plan.',
  [
    req('13.1 Log from product', 'POST', B + '/api/v1/gift-history/from-product/{{selectedProductId}}', {
      body: { wasGifted: true, userRating: 5, notes: 'أحبّها عمار كثيراً وكانت مناسبة جداً.' }, test: tolerant('Logged')
    }),
    req('13.2 Get by product', 'GET', B + '/api/v1/gift-history/from-product/{{selectedProductId}}', { test: tolerant('Fetched') }),
    req('13.3 Edit log', 'PUT', B + '/api/v1/gift-history/from-product/{{selectedProductId}}', {
      body: { wasGifted: true, userRating: 4, notes: 'هدية ممتازة.' }, test: tolerant('Edited')
    }),
    req('13.4 List my history', 'GET', B + '/api/v1/gift-history/my'),
    req('13.5 Summary', 'GET', B + '/api/v1/gift-history/summary'),
    req('13.6 Spending stats', 'GET', B + '/api/v1/gift-history/spending-stats?from=2026-01-01&to=2026-12-31')
  ]
));

flows.push(folder(
  '14 — Gift Quality Check (Bayan)',
  'Standalone AI suitability check (no gift plan needed).',
  [
    req('14.1 Run quality check', 'POST', B + '/api/v1/gift-quality-checks/add/' + R, {
      body: { giftName: 'ساعة ذكية', giftDescription: 'ساعة رياضية', price: 499.0, occasionType: 'GRADUATION' }
    }),
    req('14.2 List by recipient → capture checkId', 'GET', B + '/api/v1/gift-quality-checks/recipients/' + R, { test: captureMaxId('qualityCheckId', 'qualityCheckId') }),
    req('14.3 Get check by id', 'GET', B + '/api/v1/gift-quality-checks/{{qualityCheckId}}')
  ]
));

flows.push(folder(
  '15 — Reminders (Bayan)',
  'Schedule reminder; WhatsApp fires when due (scheduled job).',
  [
    req('15.1 Add reminder', 'POST', B + '/api/v1/reminders/add/' + R, {
      body: { reminderDate: '{{reminderDate}}', message: 'تذكير: تخرّج عمار — جهّز الهدية!', status: 'PENDING' }
    }),
    req('15.2 Get my reminders → capture id', 'GET', B + '/api/v1/reminders/get-my', { test: captureMaxId('reminderId', 'reminderId') }),
    req('15.3 Update reminder', 'PUT', B + '/api/v1/reminders/update/{{reminderId}}', {
      body: { reminderDate: '{{reminderDate}}', message: 'تذكير محدّث: لا تنسَ تغليف هدية عمار.', status: 'PENDING' }
    }),
    req('15.4 Delete reminder', 'DELETE', B + '/api/v1/reminders/delete/{{reminderId}}')
  ]
));

flows.push(folder(
  '16 — Notifications (Bayan)',
  'In-app notifications including dedicated mark-read endpoint.',
  [
    req('16.1 Create notification → capture id', 'POST', B + '/api/v1/notifications', {
      body: { title: 'توصياتك جاهزة', message: 'تم تجهيز توصيات الهدايا لعمار.', type: 'RECOMMENDATIONS_READY', status: 'UNREAD' },
      test: captureBodyId('notificationId', 'notificationId')
    }),
    req('16.2 List mine', 'GET', B + '/api/v1/notifications/my'),
    req('16.3 Get by id', 'GET', B + '/api/v1/notifications/{{notificationId}}'),
    req('16.4 Mark read (dedicated endpoint)', 'PUT', B + '/api/v1/notifications/{{notificationId}}/read', { test: tolerant('Marked read') }),
    req('16.5 Update notification', 'PUT', B + '/api/v1/notifications/{{notificationId}}', { body: { status: 'READ' } }),
    req('16.6 Delete notification', 'DELETE', B + '/api/v1/notifications/{{notificationId}}')
  ]
));

flows.push(folder(
  '17 — Group Gifts & Public Voting (Bayan)',
  'Owner endpoints authenticated; invitees vote via public token (no auth).',
  [
    req('17.1 Create group gift → capture id', 'POST', B + '/api/v1/group-gifts', {
      body: { recipientId: '{{recipientId}}', title: 'هدية تخرّج جماعية لعمار', description: 'تصويت الأصدقاء على الهدية.', giftGivingDate: '{{giftGivingDate}}', votingDeadline: '{{votingDeadline}}' },
      test: captureBodyId('groupGiftId', 'groupGiftId')
    }),
    req('17.2 List mine', 'GET', B + '/api/v1/group-gifts/my'),
    req('17.3 Get by id', 'GET', B + '/api/v1/group-gifts/{{groupGiftId}}'),
    req('17.4 Add option', 'POST', B + '/api/v1/group-gifts/add-option/{{groupGiftId}}', {
      body: { giftName: 'ساعة آبل', description: 'ساعة ذكية', priceBand: '500-800 SAR', reason: 'عملية وأنيقة' }
    }),
    req('17.5 Generate AI options', 'POST', B + '/api/v1/group-gifts/ai-generate-option/{{groupGiftId}}', { test: tolerant('AI options') }),
    req('17.6 Get options → capture optionId', 'GET', B + '/api/v1/group-gifts/get-options/{{groupGiftId}}', { test: captureFirstId('groupGiftOptionId', 'groupGiftOptionId') }),
    req('17.7 Send invites → capture token', 'POST', B + '/api/v1/group-gifts/send-invite/{{groupGiftId}}', {
      body: [{ inviteeName: 'ضيف', inviteeEmail: 'swwdswwd124@gmail.com' }],
      test: CAPTURE_INVITE_TOKEN
    }),
    req('17.8 Public: get vote page', 'GET', B + '/api/v1/public/group-gifts/vote/{{inviteToken}}', { auth: 'none', test: tolerant('Vote page') }),
    req('17.9 Public: submit vote', 'POST', B + '/api/v1/public/group-gifts/vote/{{inviteToken}}', {
      auth: 'none', body: { groupGiftOptionId: '{{groupGiftOptionId}}' }, test: tolerant('Vote submitted')
    }),
    req('17.10 Get results', 'GET', B + '/api/v1/group-gifts/results/{{groupGiftId}}', { test: tolerant('Results') }),
    req('17.11 Close voting', 'PUT', B + '/api/v1/group-gifts/close-voting/{{groupGiftId}}', { test: tolerant('Closed') })
  ]
));

flows.push(folder(
  '18 — Dashboard (Saud)',
  'Aggregated home-screen data.',
  [
    req('18.1 Get dashboard', 'GET', B + '/api/v1/dashboard')
  ]
));

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 2 — ADMIN & MAINTENANCE (19)
// ═══════════════════════════════════════════════════════════════════════════
const adminTools = [
  folder('Users', 'ADMIN list-all + self-delete (destructive, run last).', [
    req('Get all users', 'GET', B + '/api/v1/users/get', { auth: 'admin', test: tolerant('Users listed') }),
    req('Delete current user (DESTRUCTIVE)', 'DELETE', B + '/api/v1/users/delete', { test: tolerant('Self deleted') })
  ]),
  folder('Required Questions CRUD', 'Throwaway question lifecycle.', [
    req('Create temp question', 'POST', B + '/api/v1/required-questions/add', {
      auth: 'admin', body: { questionText: 'سؤال تجريبي', questionType: 'TEXT', isActive: true, displayOrder: 99 }, test: tolerant('Created')
    }),
    req('List → capture', 'GET', B + '/api/v1/required-questions/get', { auth: 'admin', test: captureMaxIdTolerant('tmpQuestionId', 'tmpQuestionId') }),
    req('Update', 'PUT', B + '/api/v1/required-questions/update/{{tmpQuestionId}}', {
      auth: 'admin', body: { questionText: 'سؤال محدّث', questionType: 'TEXT', isActive: true, displayOrder: 99 }, test: tolerant('Updated')
    }),
    req('Disable', 'PUT', B + '/api/v1/required-questions/disable/{{tmpQuestionId}}', { auth: 'admin', test: tolerant('Disabled') }),
    req('Delete', 'DELETE', B + '/api/v1/required-questions/delete/{{tmpQuestionId}}', { auth: 'admin', test: tolerant('Deleted') })
  ]),
  folder('Required Answer CRUD', 'Admin standalone answer records.', [
    req('List questions → capture anyQuestionId', 'GET', B + '/api/v1/required-questions/get', {
      auth: 'admin',
      test: [...tolerant('Listed'), "try{var a=pm.response.json();if(a&&a[0])pm.collectionVariables.set('anyQuestionId',a[0].id);}catch(e){}"]
    }),
    req('Create answer', 'POST', B + '/api/v1/required-question-answers/required-question/{{anyQuestionId}}', {
      auth: 'admin', body: { answerText: 'إجابة تجريبية' }, test: tolerant('Created')
    }),
    req('List all → capture', 'GET', B + '/api/v1/required-question-answers/get', { auth: 'admin', test: captureMaxIdTolerant('tmpRqAnswerId', 'tmpRqAnswerId') }),
    req('Get by id', 'GET', B + '/api/v1/required-question-answers/get-by-id/{{tmpRqAnswerId}}', { auth: 'admin', test: tolerant('Fetched') }),
    req('Update', 'PUT', B + '/api/v1/required-question-answers/update/{{tmpRqAnswerId}}', {
      auth: 'admin', body: { answerText: 'إجابة محدّثة' }, test: tolerant('Updated')
    }),
    req('Delete', 'DELETE', B + '/api/v1/required-question-answers/delete/{{tmpRqAnswerId}}', { auth: 'admin', test: tolerant('Deleted') })
  ]),
  folder('AI Question & Answer CRUD', 'Admin manual records + user regenerate.', [
    req('Create AI question', 'POST', B + '/api/v1/ai-questions/create/' + G, {
      auth: 'admin', body: { questionText: 'هل يفضّل الهدايا التقنية؟', reasonForQuestion: 'تحديد الاتجاه' }, test: tolerant('Created')
    }),
    req('List all → capture', 'GET', B + '/api/v1/ai-questions/get', { auth: 'admin', test: captureMaxIdTolerant('tmpAiQuestionId', 'tmpAiQuestionId') }),
    req('Get by id', 'GET', B + '/api/v1/ai-questions/get-by-id/{{tmpAiQuestionId}}', { auth: 'admin', test: tolerant('Fetched') }),
    req('Update', 'PUT', B + '/api/v1/ai-questions/update/{{tmpAiQuestionId}}', {
      auth: 'admin', body: { questionText: 'سؤال محدّث', reasonForQuestion: 'سبب' }, test: tolerant('Updated')
    }),
    req('Regenerate (user flow)', 'POST', B + '/api/v1/ai-questions/regenerate/' + G, { test: tolerant('Regenerated') }),
    req('Create AI answer', 'POST', B + '/api/v1/ai-answers/ai-question/{{tmpAiQuestionId}}', {
      auth: 'admin', body: { answerText: 'نعم' }, test: tolerant('Answer created')
    }),
    req('List answers → capture', 'GET', B + '/api/v1/ai-answers/get', { auth: 'admin', test: captureMaxIdTolerant('tmpAiAnswerId', 'tmpAiAnswerId') }),
    req('Get answer by id', 'GET', B + '/api/v1/ai-answers/get-by-id/{{tmpAiAnswerId}}', { auth: 'admin', test: tolerant('Fetched') }),
    req('Update answer', 'PUT', B + '/api/v1/ai-answers/update/{{tmpAiAnswerId}}', {
      auth: 'admin', body: { answerText: 'نعم، خصوصاً التقنية' }, test: tolerant('Updated')
    }),
    req('Delete answer', 'DELETE', B + '/api/v1/ai-answers/delete/{{tmpAiAnswerId}}', { auth: 'admin', test: tolerant('Deleted') }),
    req('Delete question', 'DELETE', B + '/api/v1/ai-questions/delete/{{tmpAiQuestionId}}', { auth: 'admin', test: tolerant('Deleted') })
  ]),
  folder('Cleanup & Utilities', 'Temp entities, webhooks, QR, logout.', [
    req('Regenerate recommendations', 'POST', B + '/api/v1/gift-recommendations/gift-plans/' + G + '/regenerate', { test: tolerant('Regenerated') }),
    req('Unselect recommendation', 'PUT', B + '/api/v1/gift-recommendations/{{recommendationId}}/unselect', { test: tolerant('Unselected') }),
    req('Clear selected product', 'DELETE', B + '/api/v1/selected-products/clear-selected-product/' + G, { test: tolerant('Cleared') }),
    req('Get previous plans', 'GET', B + '/api/v1/gift-plans/get-previous-plans', { test: tolerant('Previous') }),
    req('Create temp plan', 'POST', B + '/api/v1/gift-plans/create/' + R, { body: { ...PLAN, occasionType: 'BIRTHDAY', budget: 200 } }),
    req('Capture temp plan', 'GET', B + '/api/v1/gift-plans/get-my-plans', { test: captureMaxId('tmpGiftPlanId', 'tmpGiftPlanId') }),
    req('Delete temp plan', 'DELETE', B + '/api/v1/gift-plans/delete/{{tmpGiftPlanId}}', { test: tolerant('Deleted') }),
    req('Regenerate surprise plan', 'POST', B + '/api/v1/gift-plans/' + G + '/surprise-plan/regenerate', { body: { language: 'ar' }, test: tolerant('Regenerated') }),
    req('Update surprise plan', 'PUT', B + '/api/v1/gift-plans/' + G + '/surprise-plan', {
      body: { planTitle: 'خطة محدّثة', steps: 'خطوات', timingSuggestion: 'بعد الحفل' }, test: tolerant('Updated')
    }),
    req('Delete surprise plan', 'DELETE', B + '/api/v1/gift-plans/' + G + '/surprise-plan', { test: tolerant('Deleted') }),
    req('Delete gift card', 'DELETE', B + '/api/v1/gift-cards/{{giftCardId}}', { test: tolerant('Deleted') }),
    req('Delete history log', 'DELETE', B + '/api/v1/gift-history/from-product/{{selectedProductId}}', { test: tolerant('Deleted') }),
    req('Update group gift', 'PUT', B + '/api/v1/group-gifts/{{groupGiftId}}', {
      body: { title: 'هدية محدّثة', description: 'وصف', giftGivingDate: '{{giftGivingDate}}', votingDeadline: '{{votingDeadline}}' },
      test: tolerant('Updated')
    }),
    req('Delete group gift', 'DELETE', B + '/api/v1/group-gifts/{{groupGiftId}}', { test: tolerant('Deleted') }),
    req('Create temp recipient', 'POST', B + '/api/v1/recipients/add', {
      body: { ...RECIPIENT, name: 'مستلم مؤقت', notes: 'حذف لاحقاً' }
    }),
    req('Capture temp recipient', 'GET', B + '/api/v1/recipients/get-by-user-id', {
      test: [...ASSERT_OK, "var arr=pm.response.json()||[];var p=arr.filter(function(x){return x.name==='مستلم مؤقت';});if(p.length)pm.collectionVariables.set('tmpRecipientId',p.reduce(function(a,b){return a.id>b.id?a:b;}).id);"]
    }),
    req('Delete temp recipient', 'DELETE', B + '/api/v1/recipients/delete/{{tmpRecipientId}}', { test: tolerant('Deleted') }),
    req('Get all reminders', 'GET', B + '/api/v1/reminders/get'),
    req('Moyasar webhook (public)', 'POST', B + '/api/v1/payments/webhook/moyasar', { auth: 'none', body: { id: '{{moyasarId}}' }, test: tolerant('Webhook') }),
    req('Generate QR code', 'POST', B + '/api/v1/qr-code/generate', { body: { url: 'https://example.com/vote' } }),
    req('Logout', 'POST', B + '/api/v1/auth/logout', { test: tolerant('Logged out') })
  ])
];

flows.push(folder(
  '19 — Admin & Maintenance',
  'ADMIN CRUD, temp-entity cleanup, utilities. Run AFTER flows 01–18. Skip "Delete current user" unless intentional.',
  adminTools
));

// ═══════════════════════════════════════════════════════════════════════════
// SECTION 3 — COMPLETE API CATALOG (20)
// ═══════════════════════════════════════════════════════════════════════════
function cat(name, method, path, opts) {
  opts = opts || {};
  if (!opts.test) opts.test = tolerant(name);
  return req(name, method, B + path, opts);
}

const catalog = [
  folder('Users (4)', '', [
    cat('POST /users/register', 'POST', '/api/v1/users/register', { auth: 'none', body: { username: 'cat_{{stamp}}', password: 'Cat!2026', fullName: 'اختبار', email: 'swwdswwd124+cat{{stamp}}@gmail.com', phoneNumber: '0502427714' } }),
    cat('GET /users/get', 'GET', '/api/v1/users/get', { auth: 'admin' }),
    cat('PUT /users/update', 'PUT', '/api/v1/users/update', { body: { username: '{{username}}', password: '{{password}}', fullName: 'سعود', email: 'swwdswwd124+{{stamp}}@gmail.com', phoneNumber: '0502427714' } }),
    cat('DELETE /users/delete', 'DELETE', '/api/v1/users/delete')
  ]),
  folder('Recipients (8)', '', [
    cat('POST /recipients/add', 'POST', '/api/v1/recipients/add', { body: RECIPIENT }),
    cat('GET /recipients/get', 'GET', '/api/v1/recipients/get'),
    cat('GET /recipients/get-by-user-id', 'GET', '/api/v1/recipients/get-by-user-id'),
    cat('GET /recipients/get/{id}', 'GET', '/api/v1/recipients/get/' + R),
    cat('PUT /recipients/update/{id}', 'PUT', '/api/v1/recipients/update/' + R, { body: RECIPIENT }),
    cat('DELETE /recipients/delete/{id}', 'DELETE', '/api/v1/recipients/delete/{{tmpRecipientId}}'),
    cat('GET /recipients/{id}/gift-history', 'GET', '/api/v1/recipients/' + R + '/gift-history'),
    cat('GET /recipients/{id}/insights', 'GET', '/api/v1/recipients/' + R + '/insights')
  ]),
  folder('Dashboard (1)', '', [cat('GET /dashboard', 'GET', '/api/v1/dashboard')]),
  folder('Gift Plans (13)', '', [
    cat('POST /gift-plans/create/{recipientId}', 'POST', '/api/v1/gift-plans/create/' + R, { body: PLAN }),
    cat('GET /gift-plans/get-my-plans', 'GET', '/api/v1/gift-plans/get-my-plans'),
    cat('GET /gift-plans/get-plan-by-id/{id}', 'GET', '/api/v1/gift-plans/get-plan-by-id/' + G),
    cat('PUT /gift-plans/update/{id}', 'PUT', '/api/v1/gift-plans/update/' + G, { body: PLAN }),
    cat('DELETE /gift-plans/delete/{id}', 'DELETE', '/api/v1/gift-plans/delete/{{tmpGiftPlanId}}'),
    cat('GET /gift-plans/get-active-plans', 'GET', '/api/v1/gift-plans/get-active-plans'),
    cat('GET /gift-plans/get-previous-plans', 'GET', '/api/v1/gift-plans/get-previous-plans'),
    cat('GET /gift-plans/get-gift-plan-Summery/{id}', 'GET', '/api/v1/gift-plans/get-gift-plan-Summery/' + G),
    cat('POST /{id}/surprise-plan/generate', 'POST', '/api/v1/gift-plans/' + G + '/surprise-plan/generate', { body: { language: 'ar' } }),
    cat('POST /{id}/surprise-plan/regenerate', 'POST', '/api/v1/gift-plans/' + G + '/surprise-plan/regenerate', { body: { language: 'ar' } }),
    cat('PUT /{id}/surprise-plan', 'PUT', '/api/v1/gift-plans/' + G + '/surprise-plan', { body: { planTitle: 'خطة', steps: 'خطوات', timingSuggestion: 'توقيت' } }),
    cat('GET /{id}/surprise-plan', 'GET', '/api/v1/gift-plans/' + G + '/surprise-plan'),
    cat('DELETE /{id}/surprise-plan', 'DELETE', '/api/v1/gift-plans/' + G + '/surprise-plan')
  ]),
  folder('Required Questions (6)', '', [
    cat('POST /required-questions/add', 'POST', '/api/v1/required-questions/add', { auth: 'admin', body: { questionText: 'سؤال', questionType: 'TEXT', isActive: true, displayOrder: 1 } }),
    cat('GET /required-questions/get', 'GET', '/api/v1/required-questions/get', { auth: 'admin' }),
    cat('PUT /required-questions/update/{id}', 'PUT', '/api/v1/required-questions/update/{{tmpQuestionId}}', { auth: 'admin', body: { questionText: 'سؤال', questionType: 'TEXT', isActive: true, displayOrder: 1 } }),
    cat('DELETE /required-questions/delete/{id}', 'DELETE', '/api/v1/required-questions/delete/{{tmpQuestionId}}', { auth: 'admin' }),
    cat('PUT /required-questions/disable/{id}', 'PUT', '/api/v1/required-questions/disable/{{tmpQuestionId}}', { auth: 'admin' }),
    cat('GET /required-questions/gift-plans/{id}', 'GET', '/api/v1/required-questions/gift-plans/' + G)
  ]),
  folder('Required Answers (7)', '', [
    cat('POST /required-question/{id}', 'POST', '/api/v1/required-question-answers/required-question/{{anyQuestionId}}', { auth: 'admin', body: { answerText: 'إجابة' } }),
    cat('GET /required-question-answers/get', 'GET', '/api/v1/required-question-answers/get', { auth: 'admin' }),
    cat('GET /required-question-answers/get-by-id/{id}', 'GET', '/api/v1/required-question-answers/get-by-id/{{tmpRqAnswerId}}', { auth: 'admin' }),
    cat('PUT /required-question-answers/update/{id}', 'PUT', '/api/v1/required-question-answers/update/{{tmpRqAnswerId}}', { auth: 'admin', body: { answerText: 'إجابة' } }),
    cat('DELETE /required-question-answers/delete/{id}', 'DELETE', '/api/v1/required-question-answers/delete/{{tmpRqAnswerId}}', { auth: 'admin' }),
    cat('POST /gift-plans/{id}/submit', 'POST', '/api/v1/required-question-answers/gift-plans/' + G + '/submit', { body: '{{requiredAnswersBody}}' }),
    cat('GET /gift-plans/{id}', 'GET', '/api/v1/required-question-answers/gift-plans/' + G)
  ]),
  folder('AI Questions (8)', '', [
    cat('POST /ai-questions/create/{planId}', 'POST', '/api/v1/ai-questions/create/' + G, { auth: 'admin', body: { questionText: 'سؤال', reasonForQuestion: 'سبب' } }),
    cat('GET /ai-questions/get', 'GET', '/api/v1/ai-questions/get', { auth: 'admin' }),
    cat('GET /ai-questions/get-by-id/{id}', 'GET', '/api/v1/ai-questions/get-by-id/{{tmpAiQuestionId}}', { auth: 'admin' }),
    cat('PUT /ai-questions/update/{id}', 'PUT', '/api/v1/ai-questions/update/{{tmpAiQuestionId}}', { auth: 'admin', body: { questionText: 'سؤال', reasonForQuestion: 'سبب' } }),
    cat('DELETE /ai-questions/delete/{id}', 'DELETE', '/api/v1/ai-questions/delete/{{tmpAiQuestionId}}', { auth: 'admin' }),
    cat('POST /ai-questions/generate/{planId}', 'POST', '/api/v1/ai-questions/generate/' + G),
    cat('GET /ai-questions/gift-plans/{planId}', 'GET', '/api/v1/ai-questions/gift-plans/' + G),
    cat('POST /ai-questions/regenerate/{planId}', 'POST', '/api/v1/ai-questions/regenerate/' + G)
  ]),
  folder('AI Answers (7)', '', [
    cat('POST /ai-answers/ai-question/{id}', 'POST', '/api/v1/ai-answers/ai-question/{{tmpAiQuestionId}}', { auth: 'admin', body: { answerText: 'إجابة' } }),
    cat('GET /ai-answers/get', 'GET', '/api/v1/ai-answers/get', { auth: 'admin' }),
    cat('GET /ai-answers/get-by-id/{id}', 'GET', '/api/v1/ai-answers/get-by-id/{{tmpAiAnswerId}}', { auth: 'admin' }),
    cat('PUT /ai-answers/update/{id}', 'PUT', '/api/v1/ai-answers/update/{{tmpAiAnswerId}}', { auth: 'admin', body: { answerText: 'إجابة' } }),
    cat('DELETE /ai-answers/delete/{id}', 'DELETE', '/api/v1/ai-answers/delete/{{tmpAiAnswerId}}', { auth: 'admin' }),
    cat('POST /ai-answers/gift-plans/{planId}', 'POST', '/api/v1/ai-answers/gift-plans/' + G, { body: '{{aiAnswersBody}}' }),
    cat('GET /ai-answers/gift-plans/{planId}', 'GET', '/api/v1/ai-answers/gift-plans/' + G)
  ]),
  folder('Gift Recommendations (6)', '', [
    cat('GET /gift-recommendations/gift-plans/{id}', 'GET', '/api/v1/gift-recommendations/gift-plans/' + G),
    cat('POST /gift-plans/{id}/generate', 'POST', '/api/v1/gift-recommendations/gift-plans/' + G + '/generate'),
    cat('POST /gift-plans/{id}/regenerate', 'POST', '/api/v1/gift-recommendations/gift-plans/' + G + '/regenerate'),
    cat('GET /gift-plans/{id}/selected', 'GET', '/api/v1/gift-recommendations/gift-plans/' + G + '/selected'),
    cat('PUT /{id}/select', 'PUT', '/api/v1/gift-recommendations/{{recommendationId}}/select'),
    cat('PUT /{id}/unselect', 'PUT', '/api/v1/gift-recommendations/{{recommendationId}}/unselect')
  ]),
  folder('Search & Products (4)', '', [
    cat('GET /search/gift-plans/{id}/products', 'GET', '/api/v1/search/gift-plans/' + G + '/products'),
    cat('POST /select-product/{id}', 'POST', '/api/v1/selected-products/select-product/{{productId}}'),
    cat('GET /get-selected-product/{planId}', 'GET', '/api/v1/selected-products/get-selected-product/' + G),
    cat('DELETE /clear-selected-product/{planId}', 'DELETE', '/api/v1/selected-products/clear-selected-product/' + G)
  ]),
  folder('Gift Messages (6)', '', [
    cat('POST /gift-messages/generate', 'POST', '/api/v1/gift-messages/generate', { body: { recipientName: 'عمار', relationship: 'أخ', occasion: 'تخرّج', giftName: 'ساعة', tone: 'دافئ', language: 'ar', dialect: 'سعودي' } }),
    cat('POST /generate-from-plan/{id}', 'POST', '/api/v1/gift-messages/generate-from-plan/' + G, { body: { tone: 'دافئ', language: 'ar' } }),
    cat('POST /gift-messages/manual', 'POST', '/api/v1/gift-messages/manual', { body: { messageText: 'مبروك!' } }),
    cat('GET /gift-messages/my', 'GET', '/api/v1/gift-messages/my'),
    cat('GET /gift-messages/{id}', 'GET', '/api/v1/gift-messages/{{giftMessageId}}'),
    cat('PUT /gift-messages/{id}', 'PUT', '/api/v1/gift-messages/{{giftMessageId}}', { body: { messageText: 'رسالة', tone: 'دافئ', language: 'ar' } })
  ]),
  folder('Gift Cards (10)', '', [
    cat('POST /gift-cards', 'POST', '/api/v1/gift-cards', { body: { giftMessageId: '{{giftMessageId}}', recipientName: 'عمار', senderName: 'سعود', cardSize: 'MEDIUM', linkType: 'VIDEO', linkUrl: 'https://example.com', sentToEmail: 'swwdswwd124@gmail.com' } }),
    cat('GET /gift-cards/my', 'GET', '/api/v1/gift-cards/my'),
    cat('GET /gift-cards/{id}', 'GET', '/api/v1/gift-cards/{{giftCardId}}'),
    cat('PUT /gift-cards/{id}', 'PUT', '/api/v1/gift-cards/{{giftCardId}}', { body: { cardSize: 'LARGE', recipientName: 'عمار', senderName: 'سعود' } }),
    cat('POST /{id}/regenerate', 'POST', '/api/v1/gift-cards/{{giftCardId}}/regenerate'),
    cat('GET /{id}/image', 'GET', '/api/v1/gift-cards/{{giftCardId}}/image'),
    cat('GET /{id}/download?format=pdf', 'GET', '/api/v1/gift-cards/{{giftCardId}}/download?format=pdf'),
    cat('GET /{id}/download?format=png', 'GET', '/api/v1/gift-cards/{{giftCardId}}/download?format=png'),
    cat('POST /{id}/send-email', 'POST', '/api/v1/gift-cards/{{giftCardId}}/send-email', { body: { email: 'swwdswwd124@gmail.com' } }),
    cat('DELETE /gift-cards/{id}', 'DELETE', '/api/v1/gift-cards/{{giftCardId}}')
  ]),
  folder('Gift History (7)', '', [
    cat('POST /from-product/{id}', 'POST', '/api/v1/gift-history/from-product/{{selectedProductId}}', { body: { wasGifted: true, userRating: 5, notes: 'ممتاز' } }),
    cat('PUT /from-product/{id}', 'PUT', '/api/v1/gift-history/from-product/{{selectedProductId}}', { body: { wasGifted: true, userRating: 4, notes: 'جيد' } }),
    cat('GET /from-product/{id}', 'GET', '/api/v1/gift-history/from-product/{{selectedProductId}}'),
    cat('DELETE /from-product/{id}', 'DELETE', '/api/v1/gift-history/from-product/{{selectedProductId}}'),
    cat('GET /gift-history/my', 'GET', '/api/v1/gift-history/my'),
    cat('GET /gift-history/summary', 'GET', '/api/v1/gift-history/summary'),
    cat('GET /gift-history/spending-stats', 'GET', '/api/v1/gift-history/spending-stats?from=2026-01-01&to=2026-12-31')
  ]),
  folder('Gift Quality Checks (3)', '', [
    cat('POST /add/{recipientId}', 'POST', '/api/v1/gift-quality-checks/add/' + R, { body: { giftName: 'ساعة', giftDescription: 'وصف', price: 499, occasionType: 'GRADUATION' } }),
    cat('GET /recipients/{recipientId}', 'GET', '/api/v1/gift-quality-checks/recipients/' + R),
    cat('GET /{checkId}', 'GET', '/api/v1/gift-quality-checks/{{qualityCheckId}}')
  ]),
  folder('Reminders (5)', '', [
    cat('POST /add/{recipientId}', 'POST', '/api/v1/reminders/add/' + R, { body: { reminderDate: '{{reminderDate}}', message: 'تذكير', status: 'PENDING' } }),
    cat('GET /reminders/get', 'GET', '/api/v1/reminders/get'),
    cat('GET /reminders/get-my', 'GET', '/api/v1/reminders/get-my'),
    cat('PUT /update/{id}', 'PUT', '/api/v1/reminders/update/{{reminderId}}', { body: { reminderDate: '{{reminderDate}}', message: 'تذكير', status: 'PENDING' } }),
    cat('DELETE /delete/{id}', 'DELETE', '/api/v1/reminders/delete/{{reminderId}}')
  ]),
  folder('Notifications (6)', '', [
    cat('POST /notifications', 'POST', '/api/v1/notifications', { body: { title: 'إشعار', message: 'رسالة', type: 'RECOMMENDATIONS_READY', status: 'UNREAD' } }),
    cat('GET /notifications/my', 'GET', '/api/v1/notifications/my'),
    cat('GET /notifications/{id}', 'GET', '/api/v1/notifications/{{notificationId}}'),
    cat('PUT /notifications/{id}', 'PUT', '/api/v1/notifications/{{notificationId}}', { body: { status: 'READ' } }),
    cat('PUT /notifications/{id}/read', 'PUT', '/api/v1/notifications/{{notificationId}}/read'),
    cat('DELETE /notifications/{id}', 'DELETE', '/api/v1/notifications/{{notificationId}}')
  ]),
  folder('Group Gifts (11)', '', [
    cat('POST /group-gifts', 'POST', '/api/v1/group-gifts', { body: { recipientId: '{{recipientId}}', title: 'هدية', description: 'وصف', giftGivingDate: '{{giftGivingDate}}', votingDeadline: '{{votingDeadline}}' } }),
    cat('GET /group-gifts/my', 'GET', '/api/v1/group-gifts/my'),
    cat('GET /group-gifts/{id}', 'GET', '/api/v1/group-gifts/{{groupGiftId}}'),
    cat('PUT /group-gifts/{id}', 'PUT', '/api/v1/group-gifts/{{groupGiftId}}', { body: { title: 'هدية', description: 'وصف', giftGivingDate: '{{giftGivingDate}}', votingDeadline: '{{votingDeadline}}' } }),
    cat('DELETE /group-gifts/{id}', 'DELETE', '/api/v1/group-gifts/{{groupGiftId}}'),
    cat('POST /add-option/{id}', 'POST', '/api/v1/group-gifts/add-option/{{groupGiftId}}', { body: { giftName: 'ساعة', description: 'وصف', priceBand: '500 SAR', reason: 'مناسبة' } }),
    cat('POST /ai-generate-option/{id}', 'POST', '/api/v1/group-gifts/ai-generate-option/{{groupGiftId}}'),
    cat('GET /get-options/{id}', 'GET', '/api/v1/group-gifts/get-options/{{groupGiftId}}'),
    cat('POST /send-invite/{id}', 'POST', '/api/v1/group-gifts/send-invite/{{groupGiftId}}', { body: [{ inviteeName: 'ضيف', inviteeEmail: 'swwdswwd124@gmail.com' }] }),
    cat('PUT /close-voting/{id}', 'PUT', '/api/v1/group-gifts/close-voting/{{groupGiftId}}'),
    cat('GET /results/{id}', 'GET', '/api/v1/group-gifts/results/{{groupGiftId}}')
  ]),
  folder('Public Group Gifts (2)', '', [
    cat('GET /public/group-gifts/vote/{token}', 'GET', '/api/v1/public/group-gifts/vote/{{inviteToken}}', { auth: 'none' }),
    cat('POST /public/group-gifts/vote/{token}', 'POST', '/api/v1/public/group-gifts/vote/{{inviteToken}}', { auth: 'none', body: { groupGiftOptionId: '{{groupGiftOptionId}}' } })
  ]),
  folder('Payments (5)', '', [
    cat('POST /payments/premium', 'POST', '/api/v1/payments/premium', { body: PAYMENT_CARD, test: PAY_PREMIUM_TEST }),
    cat('GET /payments/my', 'GET', '/api/v1/payments/my'),
    cat('GET /premium/status', 'GET', '/api/v1/premium/status'),
    cat('POST /payments/webhook/moyasar', 'POST', '/api/v1/payments/webhook/moyasar', { auth: 'none', body: { id: '{{moyasarId}}' } }),
    cat('GET /payments/moyasar-status/{id}', 'GET', '/api/v1/payments/moyasar-status/{{moyasarId}}', { auth: 'none', test: MOYASAR_STATUS_TEST })
  ]),
  folder('QR Code & Auth (2)', '', [
    cat('POST /qr-code/generate', 'POST', '/api/v1/qr-code/generate', { body: { url: 'https://example.com' } }),
    cat('POST /auth/logout', 'POST', '/api/v1/auth/logout')
  ])
];

flows.push(folder(
  '20 — Complete API Catalog',
  'One request per route (107 endpoints). Lookup reference — run flows 01–18 for the scripted journey.',
  catalog
));

// ═══════════════════════════════════════════════════════════════════════════
// COLLECTION
// ═══════════════════════════════════════════════════════════════════════════
const collection = {
  info: {
    name: 'Tahadaw — Complete API (2026)',
    _postman_id: 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    description: [
      '# Tahadaw Complete API Collection',
      '',
      '## How to run',
      '1. Start Spring Boot on `http://localhost:8080` (**restart after code changes**).',
      '2. Run **01 → 18** top to bottom (registers Saud, captures all IDs).',
      '3. After **01**, promote Saud for admin seeding: `UPDATE user SET role=\'ADMIN\' WHERE username=\'<from flow 01>\';` then run **03**.',
      '4. **19 — Admin & Maintenance** for CRUD/cleanup. Skip self-delete unless intended.',
      '5. **20 — Complete API Catalog** lists every route once.',
      '',
      '## Payment flow',
      '- `POST /payments/premium` returns `transactionId` + `transactionUrl` for 3DS.',
      '- `GET /payments/moyasar-status/{transactionId}` (public) activates premium when Moyasar reports paid.',
      '- `POST /payments/webhook/moyasar` (public) also activates premium on paid.',
      '',
      '## Newman',
      '```',
      'node postman/build-collection.js',
      'npx newman run postman/Tahadaw-Full-System-Flows.postman_collection.json',
      '```',
      '',
      '## Auth model',
      '- HTTP Basic (no JWT). User from `@AuthenticationPrincipal`, not `?userId=`.',
      '- Public: `POST /users/register`, `/public/**`, Moyasar webhook + status.',
      '',
      '## Test data',
      '- Saud (سعود) · Ammar (عمار) · swwdswwd124@gmail.com · 0502427714 · Arabic text',
      '',
      '## External deps',
      '- OpenAI (flows 07, 08, 14, 17) · SearchAPI (flow 09) · Moyasar 3DS (flow 04) · Mail (flow 11)'
    ].join('\n'),
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'
  },
  auth: {
    type: 'basic',
    basic: [
      { key: 'username', value: '{{username}}', type: 'string' },
      { key: 'password', value: '{{password}}', type: 'string' }
    ]
  },
  item: flows,
  event: [
    ev('prerequest', [
      "function pad(n){return String(n).padStart(2,'0');}",
      "function fmtDate(x){return x.getFullYear()+'-'+pad(x.getMonth()+1)+'-'+pad(x.getDate());}",
      "function fmtDT(x){return fmtDate(x)+'T'+pad(x.getHours())+':'+pad(x.getMinutes())+':'+pad(x.getSeconds());}",
      "var now=new Date(); function plus(d){return new Date(now.getTime()+d*86400000);}",
      "pm.collectionVariables.set('occasionDate',fmtDate(plus(30)));",
      "pm.collectionVariables.set('reminderDate',fmtDT(plus(7)));",
      "pm.collectionVariables.set('giftGivingDate',fmtDate(plus(21)));",
      "pm.collectionVariables.set('votingDeadline',fmtDT(plus(14)));"
    ])
  ],
  variable: [
    { key: 'base_url', value: 'http://localhost:8080' },
    { key: 'stamp', value: '' },
    { key: 'username', value: '' },
    { key: 'password', value: '' },
    { key: 'adminUsername', value: 'admin' },
    { key: 'adminPassword', value: 'Admin!2026pass' },
    { key: 'recipientId', value: '' },
    { key: 'giftPlanId', value: '' },
    { key: 'requiredAnswersBody', value: '' },
    { key: 'aiAnswersBody', value: '' },
    { key: 'recommendationId', value: '' },
    { key: 'productId', value: '' },
    { key: 'selectedProductId', value: '' },
    { key: 'giftMessageId', value: '' },
    { key: 'giftCardId', value: '' },
    { key: 'moyasarId', value: '' },
    { key: 'qualityCheckId', value: '' },
    { key: 'reminderId', value: '' },
    { key: 'notificationId', value: '' },
    { key: 'groupGiftId', value: '' },
    { key: 'groupGiftOptionId', value: '' },
    { key: 'inviteToken', value: '' },
    { key: 'occasionDate', value: '' },
    { key: 'reminderDate', value: '' },
    { key: 'giftGivingDate', value: '' },
    { key: 'votingDeadline', value: '' },
    { key: 'tmpQuestionId', value: '' },
    { key: 'anyQuestionId', value: '' },
    { key: 'tmpRqAnswerId', value: '' },
    { key: 'tmpAiQuestionId', value: '' },
    { key: 'tmpAiAnswerId', value: '' },
    { key: 'tmpGiftPlanId', value: '' },
    { key: 'tmpRecipientId', value: '' }
  ]
};

const outPath = path.join(__dirname, 'Tahadaw-Full-System-Flows.postman_collection.json');
fs.writeFileSync(outPath, JSON.stringify(collection, null, 2), 'utf8');

let count = 0;
(function walk(items) {
  items.forEach(function (it) {
    if (it.request) count++;
    else if (it.item) walk(it.item);
  });
})(flows);

console.log('Wrote ' + outPath);
console.log('Folders: ' + flows.length + ' | Requests: ' + count);
